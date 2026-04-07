<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password - KINETIC Security</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <div class="container">
        <h1>Password Recovery</h1>
        <p class="text-muted">Reset your KINETIC security account password</p>

        <!-- Alert Messages (if any) -->
        <% String error = request.getParameter("error");
           if (error != null && !error.isEmpty()) { %>
            <div class="alert alert-error">
                <%= error %>
            </div>
        <% } %>
        
        <% String success = request.getParameter("success");
           if (success != null && !success.isEmpty()) { %>
            <div class="alert alert-success">
                <%= success %>
            </div>
        <% } %>

        <form id="forgotForm" action="forgot" method="post" onsubmit="return validateForm(this)">
            <!-- Email Field -->
            <div class="form-group">
                <label for="email">Email Address <span class="required">*</span></label>
                <input type="email" id="email" name="mail" placeholder="your.email@egerton.ac.ke" required>
                <div class="validation-error"></div>
                <p class="text-muted" style="font-size: 14px; margin-top: 8px;">
                    Enter the email address associated with your account
                </p>
            </div>

            <!-- New Password Field -->
            <div class="form-group">
                <label for="newPassword">New Password <span class="required">*</span></label>
                <input type="password" id="newPassword" name="newpass" placeholder="Min 8 chars, uppercase, lowercase, number, special char" required>
                <div class="validation-error"></div>
                <small class="text-muted">Must contain: uppercase, lowercase, number, and special character</small>
            </div>

            <!-- Confirm Password Field -->
            <div class="form-group">
                <label for="confirmPassword">Confirm New Password <span class="required">*</span></label>
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Re-enter your new password" required>
                <div class="validation-error"></div>
            </div>

            <!-- Submit Button -->
            <button type="submit" class="btn-primary">Reset Password</button>
        </form>

        <!-- Navigation Links -->
        <div class="links">
            <a href="login">Back to Login</a>
            <a href="register">Don't have an account?</a>
        </div>
    </div>

    <script src="js/validation.js"></script>
    <script>
        // Additional validation for password confirmation on reset
        document.getElementById('confirmPassword').addEventListener('blur', function() {
            const password = document.getElementById('newPassword').value;
            const confirmPassword = this.value;
            
            if (confirmPassword) {
                const result = validator.validatePasswordMatch(password, confirmPassword);
                if (!result.valid) {
                    validator.showError(this, result.message);
                } else {
                    validator.showSuccess(this);
                }
            }
        });
    </script>
</body>
</html>