<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reports | Digital Campus Entry Verification System</title>
    <style>
        :root {
            --primary-blue: #0d6efd;
            --secondary-grey: #6c757d;
            --success-green: #d1e7dd;
            --success-text: #0f5132;
            --border-color: #dee2e6;
            --bg-light: #f8f9fa;
            --font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
        }

        body {
            font-family: var(--font-family);
            background-color: #ffffff;
            color: #212529;
            margin: 0;
            padding: 2rem;
        }

        .container {
            max-width: 1000px;
            margin: 0 auto;
        }

        .header {
            text-align: center;
            margin-bottom: 2rem;
        }

        .header h1 {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 0.25rem;
        }

        .header p {
            color: var(--secondary-grey);
            font-size: 0.875rem;
            margin-top: 0;
        }

        .card {
            border: 1px solid var(--border-color);
            border-radius: 0.375rem;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            background-color: #fff;
        }

        .card-title {
            font-size: 1.1rem;
            font-weight: 600;
            margin-top: 0;
            margin-bottom: 1rem;
            color: #212529;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .badge {
            background-color: var(--bg-light);
            border: 1px solid var(--border-color);
            font-size: 0.75rem;
            padding: 0.2em 0.5em;
            border-radius: 4px;
            color: var(--secondary-grey);
        }

        .form-row {
            display: flex;
            gap: 1rem;
            margin-bottom: 1rem;
        }

        .form-group {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        label {
            font-size: 0.875rem;
            margin-bottom: 0.5rem;
            color: #212529;
        }

        input[type="date"], select, input[type="text"] {
            padding: 0.375rem 0.75rem;
            font-size: 1rem;
            border: 1px solid var(--border-color);
            border-radius: 0.375rem;
            outline: none;
            transition: border-color 0.15s ease-in-out;
        }

        input:focus, select:focus {
            border-color: #86b7fe;
            box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
        }

        .btn {
            display: inline-block;
            font-weight: 400;
            text-align: center;
            text-decoration: none;
            vertical-align: middle;
            cursor: pointer;
            padding: 0.375rem 0.75rem;
            font-size: 1rem;
            border-radius: 0.25rem;
            border: 1px solid transparent;
        }

        .btn-primary { background-color: var(--primary-blue); color: #fff; border-color: var(--primary-blue); }
        .btn-secondary { background-color: var(--secondary-grey); color: #fff; border-color: var(--secondary-grey); }
        
        .btn-primary:hover { background-color: #0b5ed7; }
        .btn-secondary:hover { background-color: #5c636a; }

        .table-responsive { overflow-x: auto; }
        
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.875rem;
        }

        th, td {
            padding: 0.75rem;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
        }

        th { background-color: var(--bg-light); font-weight: 600; }

        .status-badge {
            padding: 0.25rem 0.5rem;
            border-radius: 0.25rem;
            font-size: 0.75rem;
            font-weight: 600;
        }
        .status-entry { background-color: #d1e7dd; color: #0f5132; }
        .status-exit { background-color: #f8d7da; color: #842029; }
    </style>
</head>
<body>

<div class="container">
    <div class="header">
        <h1>Egerton University</h1>
        <p>Digital Campus Entry Verification System | Reports Management</p>
    </div>

    <div class="card">
        <h3 class="card-title">Generate Entry/Exit Reports</h3>
        <form id="reportForm">
            <div class="form-row">
                <div class="form-group">
                    <label>From Date</label>
                    <input type="date" id="startDate" name="startDate" required>
                </div>
                <div class="form-group">
                    <label>To Date</label>
                    <input type="date" id="endDate" name="endDate" required>
                </div>
                <div class="form-group">
                    <label>Department</label>
                    <select id="department" name="department">
                        <option value="ALL">All Departments</option>
                        <option value="Computer Science">Computer Science</option>
                        <option value="Nursing">Nursing</option>
                        <option value="Education">Education</option>
                    </select>
                </div>
            </div>
            <div class="form-row">
                <button type="button" class="btn btn-primary" onclick="fetchReports()">Generate Report</button>
                <button type="button" class="btn btn-secondary">Export to CSV</button>
            </div>
        </form>
    </div>

    <div class="card">
        <h3 class="card-title">Report Results <span class="badge" id="resultCount">0 Records</span></h3>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Reg Number</th>
                        <th>Full Name</th>
                        <th>Department</th>
                        <th>Hostel</th>
                        <th>Timestamp</th>
                        <th>Action Type</th>
                    </tr>
                </thead>
                <tbody id="reportTableBody">
                    <tr>
                        <td colspan="6" style="text-align: center; color: #6c757d;">Select filters and click Generate Report</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
    function fetchReports() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const department = document.getElementById('department').value;

        // Visual loading state
        const tbody = document.getElementById('reportTableBody');
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Loading data...</td></tr>';

        // Call our Java Servlet
        fetch(`/api/reports?startDate=${startDate}&endDate=${endDate}&department=${department}`)
            .then(response => response.json())
            .then(data => {
                tbody.innerHTML = ''; // Clear table
                document.getElementById('resultCount').innerText = `${data.length} Records`;

                if(data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">No records found for this period.</td></tr>';
                    return;
                }

                data.forEach(record => {
                    const statusClass = record.actionType === 'ENTRY' ? 'status-entry' : 'status-exit';
                    const row = `
                        <tr>
                            <td>${record.regNumber}</td>
                            <td>${record.fullName}</td>
                            <td>${record.department}</td>
                            <td>${record.hostel}</td>
                            <td>${record.timestamp}</td>
                            <td><span class="status-badge ${statusClass}">${record.actionType}</span></td>
                        </tr>
                    `;
                    tbody.innerHTML += row;
                });
            })
            .catch(error => {
                console.error('Error feetching reports:', error);
                tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: red;">Error loading reports. Check server logs.</td></tr>';
            });
    }
</script>

</body>
</html>