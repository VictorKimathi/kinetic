<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Success</title>
    <style>
        body { font-family: Segoe UI, sans-serif; margin: 2rem; background: #f6fff8; color: #173a2a; }
        .box { max-width: 700px; margin: 0 auto; border: 1px solid #9dd7b5; border-radius: 8px; padding: 1.2rem; background: #fff; }
        a { color: #0b7a49; }
    </style>
</head>
<body>
    <div class="box">
        <h2>Success</h2>
        <p><%= request.getParameter("message") == null ? "Operation completed successfully." : request.getParameter("message") %></p>
        <p><a href="index.html">Back to signup</a> | <a href="login">Go to login</a></p>
    </div>
</body>
</html>
