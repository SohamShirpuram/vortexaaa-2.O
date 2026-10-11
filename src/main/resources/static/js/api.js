/**
 * GovFix - API Communication Layer
 * Uses Vanilla Fetch API with dynamic X-GovFix-Role & X-GovFix-User headers
 */

const API = {
  // Current active role state
  currentRole: localStorage.getItem('govfix_role') || 'CITIZEN',
  currentUser: localStorage.getItem('govfix_user') || 'citizen_rahul',

  setRole(role, user) {
    this.currentRole = role;
    this.currentUser = user || this.getDefaultUserForRole(role);
    localStorage.setItem('govfix_role', this.currentRole);
    localStorage.setItem('govfix_user', this.currentUser);
    console.log(`[GovFix Auth] Switched active role to: ${this.currentRole} (${this.currentUser})`);
  },

  getDefaultUserForRole(role) {
    switch (role.toUpperCase()) {
      case 'OFFICER': return 'officer_sharma';
      case 'SUPERVISOR': return 'supervisor_kulkarni';
      case 'ADMIN': return 'admin_govfix';
      case 'AUDITOR': return 'auditor_joshi';
      default: return 'citizen_rahul';
    }
  },

  async request(endpoint, options = {}) {
    const headers = options.headers || {};
    
    // Attach simulated auth headers for Spring Security
    headers['X-GovFix-Role'] = this.currentRole;
    headers['X-GovFix-User'] = this.currentUser;

    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json';
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(endpoint, config);
      const json = await response.json();

      if (!response.ok) {
        const errorMsg = json.message || `Request failed with status ${response.status}`;
        throw new Error(errorMsg);
      }

      return json;
    } catch (err) {
      console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, err);
      throw err;
    }
  },

  // 1. System & Dashboard
  getHealth() { return this.request('/api/health'); },
  getDashboardStats() { return this.request('/api/dashboard/stats'); },

  // 2. Complaints Lifecycle
  getComplaints(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/api/complaints${query ? '?' + query : ''}`);
  },
  createComplaint(data) {
    return this.request('/api/complaints', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },
  getComplaintById(id) { return this.request(`/api/complaints/${id}`); },
  trackComplaint(ticketId) { return this.request(`/api/complaints/track/${ticketId}`); },
  getTimeline(id) { return this.request(`/api/complaints/${id}/timeline`); },
  
  executeTransition(id, transitionData) {
    return this.request(`/api/complaints/${id}/transitions`, {
      method: 'POST',
      body: JSON.stringify(transitionData)
    });
  },
  assignOfficer(id, assignData) {
    return this.request(`/api/complaints/${id}/assign`, {
      method: 'POST',
      body: JSON.stringify(assignData)
    });
  },
  recordInspection(id, inspectionData) {
    return this.request(`/api/complaints/${id}/inspection`, {
      method: 'POST',
      body: JSON.stringify(inspectionData)
    });
  },
  uploadEvidence(id, formData) {
    return this.request(`/api/complaints/${id}/evidence`, {
      method: 'POST',
      body: formData
    });
  },
  submitFeedback(id, feedbackData) {
    return this.request(`/api/complaints/${id}/feedback`, {
      method: 'POST',
      body: JSON.stringify(feedbackData)
    });
  },
  requestReopen(id, reopenData) {
    return this.request(`/api/complaints/${id}/reopen`, {
      method: 'POST',
      body: JSON.stringify(reopenData)
    });
  },

  // 3. Workflows
  getWorkflows() { return this.request('/api/workflows'); },
  createWorkflow(data) {
    return this.request('/api/workflows', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },
  updateWorkflow(id, data) {
    return this.request(`/api/workflows/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  // 4. Audit & Escalations
  getAuditLogs(ticketId = '') {
    const query = ticketId ? `?ticketId=${encodeURIComponent(ticketId)}` : '';
    return this.request(`/api/audit${query}`);
  },
  getEscalations() { return this.request('/api/escalations'); },
  manualEscalate(data) {
    return this.request('/api/escalations/manual', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 5. AI Assistant
  suggestCategory(data) {
    return this.request('/api/ai/suggest-category', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },
  submitAiDecision(data) {
    return this.request('/api/ai/decision', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },
  getPendingAiRecommendations() { return this.request('/api/ai/pending'); },

  // 6. Smart Garbage Collection Van Live Tracking (Featured PCMC Module)
  getGarbageVans() { return this.request('/api/garbage-tracking/vans'); },
  getGarbageVanById(id) { return this.request(`/api/garbage-tracking/vans/${id}`); },
  simulateGpsTick() {
    return this.request('/api/garbage-tracking/simulate-tick', { method: 'POST' });
  },
  reportMissedPickup(data) {
    return this.request('/api/garbage-tracking/report-missed', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 7. Demo Controls & Users
  advanceDemoTime(hours = 24) {
    return this.request('/api/demo/advance-time', {
      method: 'POST',
      body: JSON.stringify({ hours })
    });
  },
  resetDemo() {
    return this.request('/api/demo/reset', { method: 'POST' });
  },
  getDemoUsers() { return this.request('/api/demo/users'); }
};

