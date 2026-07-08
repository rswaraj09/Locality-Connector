/*
 * Locality Connector - lightweight client-side validation.
 * Mirrors the server-side Bean Validation constraints so users get instant feedback;
 * the server remains the source of truth.
 */
(function (window) {
  "use strict";

  var EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  function isEmail(value) {
    return EMAIL_RE.test(String(value || "").trim());
  }

  function isNonEmpty(value) {
    return String(value || "").trim().length > 0;
  }

  function minLength(value, n) {
    return String(value || "").length >= n;
  }

  function isNumberInRange(value, min, max) {
    var n = Number(value);
    return !isNaN(n) && n >= min && n <= max;
  }

  function isPositiveNumber(value) {
    var n = Number(value);
    return !isNaN(n) && n >= 0;
  }

  function validateEmail(value) {
    if (!isNonEmpty(value)) return "Email is required";
    if (!isEmail(value)) return "Enter a valid email address";
    return null;
  }

  function validatePassword(value) {
    if (!isNonEmpty(value)) return "Password is required";
    if (!minLength(value, 6)) return "Password must be at least 6 characters";
    return null;
  }

  function validateRequired(value, label) {
    if (!isNonEmpty(value)) return (label || "This field") + " is required";
    return null;
  }

  function validateRating(value) {
    if (!isNumberInRange(value, 1, 5)) return "Rating must be between 1 and 5";
    return null;
  }

  function validatePrice(value) {
    if (!isPositiveNumber(value)) return "Price must be a non-negative number";
    return null;
  }

  /* Render an inline error message next to a field. Pass null/empty to clear. */
  function showError(input, message) {
    if (!input || !input.parentNode) return;
    var holder = input.parentNode.querySelector(".field-error");
    if (!holder) {
      holder = document.createElement("div");
      holder.className = "field-error";
      input.parentNode.appendChild(holder);
    }
    holder.textContent = message || "";
    if (message) {
      input.classList.add("invalid");
    } else {
      input.classList.remove("invalid");
    }
  }

  /*
   * Validate a form from a rule map: { fieldId: validatorFn }.
   * Returns true when all validators pass, attaching inline errors otherwise.
   */
  function validateForm(form, rules) {
    var ok = true;
    Object.keys(rules).forEach(function (fieldId) {
      var input = form.querySelector("#" + fieldId) || form.elements[fieldId];
      var message = input ? rules[fieldId](input.value) : null;
      showError(input, message);
      if (message) ok = false;
    });
    return ok;
  }

  window.LCValidate = {
    isEmail: isEmail,
    isNonEmpty: isNonEmpty,
    minLength: minLength,
    isNumberInRange: isNumberInRange,
    isPositiveNumber: isPositiveNumber,
    validateEmail: validateEmail,
    validatePassword: validatePassword,
    validateRequired: validateRequired,
    validateRating: validateRating,
    validatePrice: validatePrice,
    showError: showError,
    validateForm: validateForm
  };
})(window);
