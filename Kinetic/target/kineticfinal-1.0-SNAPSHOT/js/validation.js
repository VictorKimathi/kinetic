/**
 * KINETIC Form Validation
 * Mobile-first, clear error messaging, real-time validation
 */

class KineticValidator {
    constructor() {
        this.rules = {
            email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
            phone: /^[\d\s\-\+\(\)]+$/,
            password: {
                minLength: 8,
                hasUpperCase: /[A-Z]/,
                hasLowerCase: /[a-z]/,
                hasNumbers: /\d/,
                hasSpecialChar: /[!@#$%^&*]/
            },
            firstName: { minLength: 2, maxLength: 50 },
            lastName: { minLength: 2, maxLength: 50 },
            name: { minLength: 2, maxLength: 100 },
            date: { isValidDate: true },
            department: { minLength: 2, maxLength: 50 }
        };
    }

    /**
     * Validate email format
     */
    validateEmail(email) {
        if (!email) return { valid: false, message: 'Email is required' };
        if (!this.rules.email.test(email)) {
            return { valid: false, message: 'Please enter a valid email address' };
        }
        return { valid: true };
    }

    /**
     * Validate password strength
     */
    validatePassword(password) {
        if (!password) return { valid: false, message: 'Password is required' };
        if (password.length < this.rules.password.minLength) {
            return { 
                valid: false, 
                message: `Password must be at least ${this.rules.password.minLength} characters` 
            };
        }
        if (!this.rules.password.hasUpperCase.test(password)) {
            return { valid: false, message: 'Password must contain at least one uppercase letter' };
        }
        if (!this.rules.password.hasLowerCase.test(password)) {
            return { valid: false, message: 'Password must contain at least one lowercase letter' };
        }
        if (!this.rules.password.hasNumbers.test(password)) {
            return { valid: false, message: 'Password must contain at least one number' };
        }
        if (!this.rules.password.hasSpecialChar.test(password)) {
            return { 
                valid: false, 
                message: 'Password must contain at least one special character (!@#$%^&*)' 
            };
        }
        return { valid: true };
    }

    /**
     * Validate password match
     */
    validatePasswordMatch(password, confirmPassword) {
        if (!confirmPassword) {
            return { valid: false, message: 'Please confirm your password' };
        }
        if (password !== confirmPassword) {
            return { valid: false, message: 'Passwords do not match' };
        }
        return { valid: true };
    }

    /**
     * Validate text fields (name, etc)
     */
    validateTextField(value, fieldName, min = 2, max = 100) {
        if (!value || !value.trim()) {
            return { valid: false, message: `${fieldName} is required` };
        }
        if (value.trim().length < min) {
            return { valid: false, message: `${fieldName} must be at least ${min} characters` };
        }
        if (value.trim().length > max) {
            return { valid: false, message: `${fieldName} must be no more than ${max} characters` };
        }
        return { valid: true };
    }

    /**
     * Validate phone number
     */
    validatePhone(phone) {
        if (!phone) return { valid: false, message: 'Phone number is required' };
        if (!this.rules.phone.test(phone)) {
            return { valid: false, message: 'Please enter a valid phone number' };
        }
        if (phone.replace(/\D/g, '').length < 10) {
            return { valid: false, message: 'Phone number must have at least 10 digits' };
        }
        return { valid: true };
    }

    /**
     * Validate date of birth
     */
    validateDateOfBirth(dateString) {
        if (!dateString) return { valid: false, message: 'Date of birth is required' };
        
        const date = new Date(dateString);
        const today = new Date();
        const age = today.getFullYear() - date.getFullYear();
        
        if (age < 16) {
            return { valid: false, message: 'You must be at least 16 years old' };
        }
        if (age > 120) {
            return { valid: false, message: 'Please enter a valid date of birth' };
        }
        return { valid: true };
    }

    /**
     * Validate date field
     */
    validateDate(dateString) {
        if (!dateString) return { valid: false, message: 'Date is required' };
        
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return { valid: false, message: 'Invalid date format' };
        }
        return { valid: true };
    }

    /**
     * Validate select field
     */
    validateSelect(value, fieldName) {
        if (!value || value === '') {
            return { valid: false, message: `${fieldName} is required` };
        }
        return { valid: true };
    }

    /**
     * Show validation message
     */
    showError(element, message) {
        element.classList.add('error');
        element.classList.remove('success');
        
        let errorElement = element.parentElement.querySelector('.validation-error');
        if (!errorElement) {
            errorElement = document.createElement('div');
            errorElement.className = 'validation-error';
            element.parentElement.appendChild(errorElement);
        }
        
        errorElement.textContent = message;
        errorElement.classList.add('show');
    }

    /**
     * Show success state
     */
    showSuccess(element) {
        element.classList.remove('error');
        element.classList.add('success');
        
        const errorElement = element.parentElement.querySelector('.validation-error');
        if (errorElement) {
            errorElement.classList.remove('show');
        }
        
        const successElement = element.parentElement.querySelector('.validation-success');
        if (successElement) {
            successElement.classList.add('show');
        }
    }

    /**
     * Clear validation messages
     */
    clearValidation(element) {
        element.classList.remove('error', 'success');
        
        const errorElement = element.parentElement.querySelector('.validation-error');
        if (errorElement) {
            errorElement.classList.remove('show');
        }
        
        const successElement = element.parentElement.querySelector('.validation-success');
        if (successElement) {
            successElement.classList.remove('show');
        }
    }
}

// Initialize validator globally
const validator = new KineticValidator();

function getFieldLabel(element) {
    const explicit = element.getAttribute('data-label');
    if (explicit) return explicit;

    const id = element.id;
    if (id) {
        const label = document.querySelector('label[for="' + id + '"]');
        if (label) {
            return label.textContent.replace('*', '').trim();
        }
    }

    if (element.name) {
        return element.name.replace(/([A-Z])/g, ' $1').replace(/^./, function (m) { return m.toUpperCase(); });
    }

    return 'Field';
}

function getPasswordSourceField(element) {
    if (!element.name) return null;
    const name = element.name.toLowerCase();
    if (name.indexOf('confirm') === -1) return null;

    const form = element.form;
    if (!form) return null;

    return form.querySelector('input[name="pass"], input[name="password"], input[name="newpass"], input[name="newPassword"]');
}

function getValidatableFields(formElement) {
    const fields = formElement.querySelectorAll('input, select, textarea');
    return Array.prototype.filter.call(fields, function (field) {
        if (field.disabled) return false;
        if (field.type === 'hidden' || field.type === 'submit' || field.type === 'button' || field.type === 'reset') return false;
        if (field.hasAttribute('data-no-validate')) return false;
        return true;
    });
}

function validateSingleField(field) {
    const value = (field.value || '').trim();
    const fieldType = (field.type || '').toLowerCase();
    const fieldName = getFieldLabel(field);
    const required = field.required;

    if (required && !value) {
        validator.showError(field, fieldName + ' is required');
        return false;
    }

    if (!value) {
        validator.clearValidation(field);
        return true;
    }

    if (fieldType === 'email') {
        const result = validator.validateEmail(value);
        if (!result.valid) {
            validator.showError(field, result.message);
            return false;
        }
    }

    if (fieldType === 'password') {
        if (field.minLength && value.length < field.minLength) {
            validator.showError(field, fieldName + ' must be at least ' + field.minLength + ' characters');
            return false;
        }
        if (!field.minLength) {
            const result = validator.validatePassword(value);
            if (!result.valid) {
                validator.showError(field, result.message);
                return false;
            }
        }
    }

    if (fieldType === 'tel' || (field.name && field.name.toLowerCase().indexOf('phone') !== -1)) {
        const digits = value.replace(/\D/g, '');
        if (digits.length < 10 || digits.length > 15) {
            validator.showError(field, 'Phone number must be between 10 and 15 digits');
            return false;
        }
    }

    if (fieldType === 'number') {
        const number = Number(value);
        if (Number.isNaN(number)) {
            validator.showError(field, fieldName + ' must be a number');
            return false;
        }
        if (field.min !== '' && number < Number(field.min)) {
            validator.showError(field, fieldName + ' must be at least ' + field.min);
            return false;
        }
        if (field.max !== '' && number > Number(field.max)) {
            validator.showError(field, fieldName + ' must be at most ' + field.max);
            return false;
        }
    }

    if (fieldType === 'date') {
        const result = field.name === 'dateOfBirth' ? validator.validateDateOfBirth(value) : validator.validateDate(value);
        if (!result.valid) {
            validator.showError(field, result.message);
            return false;
        }
    }

    if (field.tagName.toLowerCase() === 'select') {
        const result = validator.validateSelect(value, fieldName);
        if (!result.valid) {
            validator.showError(field, result.message);
            return false;
        }
    }

    const sourcePassword = getPasswordSourceField(field);
    if (sourcePassword) {
        const result = validator.validatePasswordMatch(sourcePassword.value, value);
        if (!result.valid) {
            validator.showError(field, result.message);
            return false;
        }
    }

    validator.showSuccess(field);
    return true;
}

/**
 * Setup real-time validation on form fields
 */
function setupRealtimeValidation() {
    const forms = document.querySelectorAll('form');

    forms.forEach(function (form) {
        const fields = getValidatableFields(form);

        fields.forEach(function (field) {
            field.addEventListener('blur', function () {
                validateSingleField(field);
            });

            field.addEventListener('input', function () {
                if (field.classList.contains('error')) {
                    validateSingleField(field);
                }
            });

            field.addEventListener('change', function () {
                validateSingleField(field);
            });
        });

        form.addEventListener('submit', function (event) {
            let formValid = true;
            let firstInvalid = null;

            fields.forEach(function (field) {
                const valid = validateSingleField(field);
                if (!valid) {
                    formValid = false;
                    if (!firstInvalid) {
                        firstInvalid = field;
                    }
                }
            });

            if (!formValid) {
                event.preventDefault();
                if (firstInvalid) {
                    firstInvalid.focus();
                }
            }
        });
    });
}

/**
 * Validate entire form before submission
 */
function validateForm(formElement) {
    let isValid = true;
    const fields = getValidatableFields(formElement);
    fields.forEach(function (field) {
        if (!validateSingleField(field)) {
            isValid = false;
        }
    });
    return isValid;
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', setupRealtimeValidation);
