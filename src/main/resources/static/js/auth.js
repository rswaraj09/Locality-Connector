/*
 * Locality Connector - client-side JWT auth helper.
 *
 * The backend is fully stateless: login returns a JWT which we keep in localStorage
 * and attach as `Authorization: Bearer <token>` on every protected API call. There is
 * no server session, so page access is guarded on the client via requireRole().
 */
(function (window) {
  "use strict";

  var TOKEN_KEY = "lc_token";
  var IDENTITY_KEY = "lc_identity";

  function setSession(token, identity) {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    }
    if (identity) {
      localStorage.setItem(IDENTITY_KEY, JSON.stringify(identity));
    }
  }

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function getIdentity() {
    try {
      return JSON.parse(localStorage.getItem(IDENTITY_KEY) || "null");
    } catch (e) {
      return null;
    }
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(IDENTITY_KEY);
  }

  function isLoggedIn() {
    return !!getToken();
  }

  function hasRole(role) {
    var identity = getIdentity();
    return !!(identity && Array.isArray(identity.roles) && identity.roles.indexOf(role) !== -1);
  }

  function authHeaders(extra) {
    var headers = Object.assign({ "Content-Type": "application/json" }, extra || {});
    var token = getToken();
    if (token) {
      headers["Authorization"] = "Bearer " + token;
    }
    return headers;
  }

  /* fetch() wrapper that injects the bearer token and clears the session on 401. */
  function authFetch(url, options) {
    options = options || {};
    options.headers = authHeaders(options.headers);
    return fetch(url, options).then(function (res) {
      if (res.status === 401) {
        clearSession();
      }
      return res;
    });
  }

  /* role: "user" | "business". Resolves with the login payload or rejects with an Error. */
  function login(role, email, password) {
    return fetch("/api/auth/" + role + "/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email, password: password })
    }).then(function (res) {
      if (res.status === 429) {
        var retry = res.headers.get("Retry-After");
        return res.json().catch(function () { return {}; }).then(function (body) {
          throw new Error((body && body.error) || ("Too many attempts. Try again in " + retry + "s"));
        });
      }
      return res.json().then(function (body) {
        if (!res.ok || !body.success) {
          throw new Error((body && body.error) || "Login failed");
        }
        var data = body.data || {};
        setSession(data.token, {
          id: data.userId || data.businessId,
          name: data.name || data.businessName,
          email: data.email,
          roles: data.roles || []
        });
        return data;
      });
    });
  }

  function logout(redirectTo) {
    return authFetch("/api/auth/logout", { method: "POST" })
      .catch(function () { /* ignore network errors during logout */ })
      .then(function () {
        clearSession();
        if (redirectTo) {
          window.location.href = redirectTo;
        }
      });
  }

  /* Client-side page guard. Redirects to loginUrl when not authenticated / wrong role. */
  function requireRole(role, loginUrl) {
    if (!isLoggedIn() || (role && !hasRole(role))) {
      window.location.href = loginUrl || "/";
      return false;
    }
    return true;
  }

  window.LCAuth = {
    setSession: setSession,
    getToken: getToken,
    getIdentity: getIdentity,
    clearSession: clearSession,
    isLoggedIn: isLoggedIn,
    hasRole: hasRole,
    authHeaders: authHeaders,
    authFetch: authFetch,
    login: login,
    logout: logout,
    requireRole: requireRole
  };
})(window);
