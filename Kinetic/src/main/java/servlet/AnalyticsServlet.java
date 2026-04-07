package servlet;

import com.egerton.entryloggingsystem.dao.DAO;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AnalyticsServlet extends HttpServlet {

    private final DAO dao = new DAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ctx = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(ctx + "/jsp/auth/login.jsp?error=Please+login+first");
            return;
        }
        String role = (String) session.getAttribute("role");
        if (role == null || !"GUARD".equalsIgnoreCase(role)) {
            response.sendRedirect(ctx + "/jsp/auth/login.jsp?error=Guards+only");
            return;
        }

        String search = request.getParameter("search");
        if (search == null) {
            search = "";
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);

        List<Map<String, Object>> allLogs = dao.getAllEntryLogs();
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        for (Map<String, Object> row : allLogs) {
            String gate = row.get("entry_gate") == null ? "" : String.valueOf(row.get("entry_gate")).toLowerCase(Locale.ROOT);
            if (needle.isEmpty() || gate.contains(needle)) {
                results.add(row);
            }
        }

        // Build last-7-days volume chart data.
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        List<String> dayLabels = new ArrayList<String>();
        List<Integer> dayCounts = new ArrayList<Integer>();
        Map<LocalDate, Integer> dailyCounter = new HashMap<LocalDate, Integer>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = startDate.plusDays(i);
            dayLabels.add(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ROOT));
            dailyCounter.put(d, 0);
        }

        Map<String, Integer> gateCounter = new HashMap<String, Integer>();
        int[][] heatCounts = new int[7][9];
        String[] hourLabels = new String[] {"06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00"};

        for (Map<String, Object> row : allLogs) {
            LocalDateTime entryDateTime = getEntryDateTime(row.get("entry_time"));
            if (entryDateTime == null) {
                continue;
            }

            LocalDate entryDate = entryDateTime.toLocalDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                dailyCounter.put(entryDate, dailyCounter.get(entryDate) + 1);
            }

            String gate = safeText(row.get("entry_gate"));
            if (gate.isEmpty()) {
                gate = "Unknown Gate";
            }
            gateCounter.put(gate, gateCounter.containsKey(gate) ? gateCounter.get(gate) + 1 : 1);

            int dayIndex = entryDateTime.getDayOfWeek().getValue() - 1;
            int hour = entryDateTime.getHour();
            int slot = (hour - 6) / 2;
            if (dayIndex >= 0 && dayIndex < 7 && slot >= 0 && slot < 9) {
                heatCounts[dayIndex][slot] = heatCounts[dayIndex][slot] + 1;
            }
        }

        int maxDailyCount = 1;
        for (int i = 0; i < 7; i++) {
            LocalDate d = startDate.plusDays(i);
            int count = dailyCounter.get(d);
            dayCounts.add(count);
            if (count > maxDailyCount) {
                maxDailyCount = count;
            }
        }

        List<Entry<String, Integer>> gates = new ArrayList<Entry<String, Integer>>(gateCounter.entrySet());
        Collections.sort(gates, new Comparator<Entry<String, Integer>>() {
            @Override
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        String[] donutColors = new String[] {"#2e7d32", "#1b5e20", "#81c784", "#c8e6c9"};
        List<Map<String, Object>> gateLegend = new ArrayList<Map<String, Object>>();
        StringBuilder donutGradient = new StringBuilder("conic-gradient(");
        int donutTotal = 0;
        int gateSliceCount = Math.min(4, gates.size());
        for (int i = 0; i < gateSliceCount; i++) {
            donutTotal += gates.get(i).getValue();
        }
        if (donutTotal == 0) {
            donutGradient.append("#2e7d32 0 100%");
        } else {
            double from = 0.0;
            for (int i = 0; i < gateSliceCount; i++) {
                Entry<String, Integer> g = gates.get(i);
                double pct = (g.getValue() * 100.0) / donutTotal;
                double to = from + pct;
                donutGradient.append(donutColors[i]).append(" ").append(Math.round(from)).append("% ").append(Math.round(to)).append("%");
                if (i < gateSliceCount - 1) {
                    donutGradient.append(", ");
                }

                Map<String, Object> item = new HashMap<String, Object>();
                item.put("name", g.getKey());
                item.put("count", g.getValue());
                item.put("colorClass", "green" + (i + 1));
                gateLegend.add(item);
                from = to;
            }
        }
        donutGradient.append(")");

        int topGatePercent = 0;
        if (gateSliceCount > 0 && donutTotal > 0) {
            topGatePercent = (int) Math.round((gates.get(0).getValue() * 100.0) / donutTotal);
        }

        int maxHeat = 0;
        for (int d = 0; d < 7; d++) {
            for (int h = 0; h < 9; h++) {
                if (heatCounts[d][h] > maxHeat) {
                    maxHeat = heatCounts[d][h];
                }
            }
        }
        int[][] heatLevels = new int[7][9];
        for (int d = 0; d < 7; d++) {
            for (int h = 0; h < 9; h++) {
                if (maxHeat == 0) {
                    heatLevels[d][h] = 0;
                } else {
                    heatLevels[d][h] = (int) Math.round((heatCounts[d][h] * 4.0) / maxHeat);
                    if (heatLevels[d][h] < 0) {
                        heatLevels[d][h] = 0;
                    } else if (heatLevels[d][h] > 4) {
                        heatLevels[d][h] = 4;
                    }
                }
            }
        }

        List<String> weekDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

        request.setAttribute("results", results);
        request.setAttribute("search", search.trim());
        request.setAttribute("studentCount", results.size());
        request.setAttribute("totalEntries", allLogs.size());
        request.setAttribute("dayLabels", dayLabels);
        request.setAttribute("dayCounts", dayCounts);
        request.setAttribute("maxDailyCount", maxDailyCount);
        request.setAttribute("donutGradient", donutGradient.toString());
        request.setAttribute("topGatePercent", topGatePercent);
        request.setAttribute("gateLegend", gateLegend);
        request.setAttribute("hourLabels", Arrays.asList(hourLabels));
        request.setAttribute("weekDays", weekDays);
        request.setAttribute("heatLevels", heatLevels);
        request.getRequestDispatcher("/jsp/officer/analytics.jsp").forward(request, response);
    }

    private LocalDateTime getEntryDateTime(Object value) {
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        return null;
    }

    private String safeText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}