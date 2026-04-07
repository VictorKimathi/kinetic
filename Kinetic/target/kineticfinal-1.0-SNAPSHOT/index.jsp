<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    boolean loggedIn = session != null && session.getAttribute("userId") != null;
    String actionHref = loggedIn ? request.getContextPath() + "/logout" : request.getContextPath() + "/login";
    String actionLabel = loggedIn ? "Logout" : "Login";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>KINETIC</title>
    <style>
        :root {
            --ink: #ffffff;
            --muted: #dcf3e7;
            --line: rgba(255, 255, 255, 0.45);
            --card: #ffffff;
            --soft: #eefaf3;
            --text: #183226;
            --accent: #0b8b47;
            --accent-dark: #066233;
            --accent-soft: #e2f4e9;
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: "Consolas", "Courier New", monospace;
            background: linear-gradient(180deg, #0f9a53 0%, #0b7f44 48%, #ffffff 100%);
            color: var(--ink);
        }

        .hero {
            min-height: 70vh;
            display: grid;
            place-items: center;
            text-align: center;
            padding: 28px 16px;
            position: relative;
        }

        .hero::before,
        .hero::after {
            content: "";
            position: absolute;
            inset: 20px 8px auto auto;
            width: 120px;
            height: 70%;
            pointer-events: none;
            opacity: 0.18;
            background-image:
                repeating-linear-gradient(
                    to bottom,
                    rgba(220, 243, 231, 0.9) 0,
                    rgba(220, 243, 231, 0.9) 1px,
                    transparent 1px,
                    transparent 18px
                );
        }

        .hero::after {
            inset: 20px auto auto 8px;
        }

        .hero-inner {
            max-width: 700px;
            z-index: 2;
        }

        h1 {
            margin: 0;
            font-size: clamp(32px, 7vw, 74px);
            line-height: 1.02;
            letter-spacing: 0.02em;
        }

        .sub {
            margin: 18px auto 0;
            max-width: 580px;
            color: var(--muted);
            font-size: 14px;
            line-height: 1.6;
        }

        .hero-actions {
            margin-top: 22px;
            display: flex;
            justify-content: center;
            gap: 10px;
            flex-wrap: wrap;
        }

        .btn {
            text-decoration: none;
            border: 1px solid var(--line);
            color: #fff;
            background: rgba(255, 255, 255, 0.2);
            padding: 10px 14px;
            border-radius: 8px;
            font-size: 13px;
        }

        .btn.primary {
            background: var(--accent);
            border-color: var(--accent);
        }

        .btn.primary:hover { background: var(--accent-dark); }

        .section {
            background: var(--card);
            color: var(--text);
            border-top: 1px solid #cae9d7;
            padding: 44px 18px;
        }

        .wrap {
            max-width: 1050px;
            margin: 0 auto;
        }

        .kicker {
            color: #2d7a53;
            margin: 0 0 12px;
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }

        .title {
            margin: 0;
            font-size: clamp(28px, 4vw, 46px);
            line-height: 1.1;
        }

        .summary {
            margin: 14px 0 0;
            max-width: 620px;
            color: #3c5f4c;
            line-height: 1.6;
            font-size: 14px;
        }

        .metrics {
            margin-top: 30px;
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 12px;
        }

        .metric {
            border: 1px solid #c8e6d5;
            border-radius: 10px;
            background: #fff;
            padding: 16px;
        }

        .metric strong {
            display: block;
            font-size: 34px;
            line-height: 1;
            margin-bottom: 8px;
        }

        .metric span {
            font-size: 13px;
            color: #4e6f5e;
        }

        .ecosystem {
            margin-top: 34px;
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 12px;
        }

        .eco {
            background: var(--soft);
            border: 1px solid #cde8da;
            border-radius: 10px;
            padding: 14px;
            font-size: 13px;
            color: #355646;
        }

        @media (max-width: 800px) {
            .metrics, .ecosystem { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
    <section class="hero">
        <div class="hero-inner">
            <h1>Kinetic<br>Security System</h1>
            <p class="sub">
                KINETIC secures campus movement with identity checks, real-time entry logging,
                and role-based control for guards, administrators, and students.
            </p>
            <div class="hero-actions">
                <a class="btn primary" href="<%= actionHref %>"><%= actionLabel %></a>
                <% if (!loggedIn) { %>
                <a class="btn" href="<%= request.getContextPath() %>/register">Register</a>
                <% } %>
            </div>
        </div>
    </section>

    <section class="section">
        <div class="wrap">
            <p class="kicker">KINETIC Security</p>
            <h2 class="title">All-in-One Access System<br>for Total Protection</h2>
            <p class="summary">
                From gate verification to visitor tracking and complete session history,
                KINETIC gives your institution one control layer for daily movement.
            </p>

            <div class="metrics">
                <div class="metric"><strong>1,200+</strong><span>Entries Processed</span></div>
                <div class="metric"><strong>300+</strong><span>Student Records</span></div>
                <div class="metric"><strong>50+</strong><span>Security Reports</span></div>
            </div>

            <div class="ecosystem">
                <div class="eco">Student verification and registration lookup</div>
                <div class="eco">Visitor entry and overdue tracking</div>
                <div class="eco">Role dashboards for guard, admin, and student</div>
            </div>
        </div>
    </section>
</body>
</html>
