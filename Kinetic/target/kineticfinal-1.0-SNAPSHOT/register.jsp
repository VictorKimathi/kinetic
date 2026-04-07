<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - KINETIC Security</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <div class="container">
        <h1>Create Your Account</h1>
        <p class="text-muted">Join the KINETIC security system at Egerton University</p>

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

        <form id="registerForm" action="register" method="post" onsubmit="return validateForm(this)">
            <!-- Row 1: First Name & Last Name -->
            <div class="form-row">
                <div class="form-group">
                    <label for="firstName">First Name <span class="required">*</span></label>
                    <input type="text" id="firstName" name="firstName" placeholder="Enter your first name" required>
                    <div class="validation-error"></div>
                </div>
                <div class="form-group">
                    <label for="lastName">Last Name <span class="required">*</span></label>
                    <input type="text" id="lastName" name="lastName" placeholder="Enter your last name" required>
                    <div class="validation-error"></div>
                </div>
            </div>

            <!-- Row 2: Email -->
            <div class="form-group">
                <label for="email">Email Address <span class="required">*</span></label>
                <input type="email" id="email" name="mail" placeholder="your.email@egerton.ac.ke" required>
                <div class="validation-error"></div>
            </div>

            <!-- Row 3: Phone & User Type -->
            <div class="form-row">
                <div class="form-group">
                    <label for="phone">Phone Number</label>
                    <input type="tel" id="phone" name="phone" placeholder="e.g., +254 7XX XXX XXX">
                    <div class="validation-error"></div>
                </div>
                <div class="form-group">
                    <label for="userType">Role</label>
                    <select id="userType" name="userType">
                        <option value="">-- Select Role --</option>
                        <option value="security_officer">Guard</option>
                        <option value="admin">Administrator</option>
                        <option value="student">Student</option>
                    </select>
                </div>
            </div>

            <!-- Row 4: Date of Birth & Department -->
            <div class="form-row">
                <div class="form-group">
                    <label for="dateOfBirth">Date of Birth</label>
                    <input type="date" id="dateOfBirth" name="dateOfBirth">
                    <div class="validation-error"></div>
                </div>
                <div class="form-group">
                    <label for="department">Department</label>
                    <select id="department" name="department">
                        <option value="">-- Select Department --</option>
                        <option value="security">Security</option>
                        <option value="operations">Operations</option>
                        <option value="administration">Administration</option>
                        <option value="it">IT</option>
                        <option value="other">Other</option>
                    </select>
                </div>
            </div>

            <!-- Row 5: Profile Photo -->
            <div class="form-group">
                <label for="profilePhoto">Profile Photo URL</label>
                <input type="text" id="profilePhoto" name="profilePhoto" placeholder="https://example.com/photo.jpg">
            </div>

            <!-- Row 6: Password -->
            <div class="form-group">
                <label for="password">Password <span class="required">*</span></label>
                <input type="password" id="password" name="pass" placeholder="Min 8 chars, uppercase, lowercase, number, special char" required>
                <div class="validation-error"></div>
                <small class="text-muted">Must contain: uppercase, lowercase, number, and special character</small>
            </div>

            <!-- Row 7: Confirm Password -->
            <div class="form-group">
                <label for="confirmPassword">Confirm Password <span class="required">*</span></label>
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Re-enter your password" required>
                <div class="validation-error"></div>
            </div>

            <!-- Submit Button -->
            <button type="submit" class="btn-primary">Create Account</button>
        </form>

        <!-- Links -->
        <div class="links">
            <a href="login">Already have an account? Login</a>
        </div>
    </div>

    <script src="js/validation.js"></script>
    <script>
        (function () {
            const params = new URLSearchParams(window.location.search);
            const error = params.get('error');
            const debug = params.get('debug');

            if (error) {
                console.error('[KINETIC][Register] Error:', error);
            }
            if (debug) {
                console.error('[KINETIC][Register] Debug detail:', debug);
            }
        })();

        // Additional validation for password confirmation
        document.getElementById('confirmPassword').addEventListener('blur', function() {
            const password = document.getElementById('password').value;
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