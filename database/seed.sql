-- ==============================================================
-- GovFix: MySQL 8 Seed Data
-- Database: govfix
-- Realistic Fictional Data for Pimpri Chinchwad Municipal Area
-- ==============================================================

USE govfix;

-- 1. Insert Demo Users (BCrypt encoded or plain for demo: password is 'password123')
INSERT INTO users (id, username, password_hash, full_name, email, phone, role, department, is_active) VALUES
(1, 'citizen_rahul', '{noop}password123', 'Rahul Deshmukh', 'rahul.deshmukh@example.com', '9822012345', 'CITIZEN', 'Citizen', true),
(2, 'officer_sharma', '{noop}password123', 'Amit Sharma (JE Road Dept)', 'amit.sharma@pcmc-demo.gov.in', '9822023456', 'OFFICER', 'Roads and Infrastructure', true),
(3, 'officer_patil', '{noop}password123', 'Sunita Patil (Health Inspector)', 'sunita.patil@pcmc-demo.gov.in', '9822034567', 'OFFICER', 'Waste Management', true),
(4, 'supervisor_kulkarni', '{noop}password123', 'Dr. Anand Kulkarni (Executive Engineer)', 'anand.kulkarni@pcmc-demo.gov.in', '9822045678', 'SUPERVISOR', 'Public Works & Sanitation', true),
(5, 'admin_govfix', '{noop}password123', 'System Administrator', 'admin@pcmc-demo.gov.in', '9822056789', 'ADMIN', 'Municipal Administration', true),
(6, 'auditor_joshi', '{noop}password123', 'Vikas Joshi (Vigilance Officer)', 'vikas.joshi@pcmc-demo.gov.in', '9822067890', 'AUDITOR', 'Chief Auditor Office', true);

-- 2. Insert Workflow Definitions
INSERT INTO workflow_definitions (id, workflow_key, name, description, initial_state_key, category_pattern, is_active) VALUES
(1, 'pothole_workflow', 'Roads & Potholes Resolution Workflow', 'Standard 8-stage civic lifecycle with mandatory before/after photo inspection', 'Submitted', 'Roads and potholes', true),
(2, 'water_supply_workflow', 'Water Supply & Contamination Workflow', 'Fast-tracked civic pipeline for pipeline leakage and drinking water quality', 'Submitted', 'Water supply', true);

-- 3. Workflow States for Pothole Workflow
INSERT INTO workflow_states (workflow_id, state_key, display_name, stage_order, deadline_hours, is_terminal, requires_evidence, color_code) VALUES
(1, 'Submitted', 'Submitted', 1, 24, false, false, '#64748b'),
(1, 'Under Review', 'Under Review', 2, 24, false, false, '#3b82f6'),
(1, 'Assigned', 'Assigned', 3, 24, false, false, '#0284c7'),
(1, 'Inspection Required', 'Inspection Required', 4, 24, false, false, '#eab308'),
(1, 'In Progress', 'In Progress', 5, 48, false, false, '#f97316'),
(1, 'Resolution Submitted', 'Resolution Submitted', 6, 24, false, true, '#8b5cf6'),
(1, 'Resolved', 'Resolved', 7, 48, false, false, '#10b981'),
(1, 'Closed', 'Closed', 8, 0, true, false, '#0f766e'),
(1, 'Information Required', 'Information Required', 9, 48, false, false, '#ec4899'),
(1, 'Rejected', 'Rejected', 10, 0, true, false, '#ef4444'),
(1, 'Reopened', 'Reopened', 11, 24, false, false, '#dc2626'),
(1, 'Escalated', 'Escalated', 12, 12, false, false, '#b91c1c');

-- 4. Workflow Transitions for Pothole Workflow
INSERT INTO workflow_transitions (workflow_id, from_state_key, to_state_key, action_name, allowed_roles, requires_notes, requires_evidence, requires_approval) VALUES
(1, 'Submitted', 'Under Review', 'Initiate Review', 'OFFICER,SUPERVISOR,ADMIN', false, false, false),
(1, 'Under Review', 'Assigned', 'Assign Field Officer', 'SUPERVISOR,ADMIN', true, false, false),
(1, 'Under Review', 'Rejected', 'Reject Invalid Complaint', 'SUPERVISOR,ADMIN', true, false, true),
(1, 'Assigned', 'Inspection Required', 'Request Site Inspection', 'OFFICER,SUPERVISOR', false, false, false),
(1, 'Inspection Required', 'In Progress', 'Record Initial Inspection & Begin Work', 'OFFICER', true, true, false),
(1, 'In Progress', 'Resolution Submitted', 'Submit Work Completion Evidence', 'OFFICER', true, true, false),
(1, 'Resolution Submitted', 'Resolved', 'Approve Quality & Mark Resolved', 'SUPERVISOR,ADMIN', true, false, true),
(1, 'Resolution Submitted', 'In Progress', 'Reject Resolution (Rework Needed)', 'SUPERVISOR,ADMIN', true, false, false),
(1, 'Resolved', 'Closed', 'Confirm Citizen Satisfaction', 'CITIZEN,ADMIN', false, false, false),
(1, 'Resolved', 'Reopened', 'Citizen Reopen Request', 'CITIZEN,ADMIN', true, false, false),
(1, 'Reopened', 'In Progress', 'Acknowledge Reopened Case', 'SUPERVISOR,ADMIN', true, false, false),
(1, 'Submitted', 'Escalated', 'Escalate Stalled Ticket', 'SUPERVISOR,ADMIN', true, false, false),
(1, 'Assigned', 'Escalated', 'Escalate Stalled Ticket', 'SUPERVISOR,ADMIN', true, false, false),
(1, 'In Progress', 'Escalated', 'Escalate Stalled Ticket', 'SUPERVISOR,ADMIN', true, false, false),
(1, 'Escalated', 'In Progress', 'De-escalate & Resume Work', 'SUPERVISOR,ADMIN', true, false, false);

-- 5. Seed 10+ Fictional Complaints Across Diverse States in PCMC
INSERT INTO complaints (id, ticket_id, title, description, category, department, locality, ward_or_zone, landmark, priority, status, citizen_name, citizen_contact, assigned_officer_id, photo_url, deadline_at, is_escalated, created_at) VALUES
(1, 'GV-1001', 'Severe deep crater pothole near Akurdi Railway Station', 'Two-wheeler riders skidding due to a 2-foot wide pothole right outside station exit gate.', 'Roads and potholes', 'Roads and Infrastructure', 'Akurdi', 'Ward 14 (Nigdi Pradhikaran)', 'Opposite Railway Gate 2', 'HIGH', 'Inspection Required', 'Rahul Deshmukh', '9822012345', 2, 'https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600', DATE_ADD(NOW(), INTERVAL 18 HOUR), false, DATE_SUB(NOW(), INTERVAL 24 HOUR)),
(2, 'GV-1002', 'Broken water distribution pipeline leaking clean water', 'Continuous leakage on main DP road causing water wastage and low pressure in nearby housing societies.', 'Water supply', 'Water Supply & Quality', 'Wakad', 'Ward 25 (Wakad - Hinjawadi)', 'Near Dutta Mandir Chowk', 'CRITICAL', 'In Progress', 'Pooja Jadhav', '9822098765', 2, 'https://images.unsplash.com/photo-1541888946425-d0fbb18f15f7?w=600', DATE_SUB(NOW(), INTERVAL 4 HOUR), true, DATE_SUB(NOW(), INTERVAL 52 HOUR)),
(3, 'GV-1003', 'Overflowing community garbage dumpster near vegetable market', 'Waste not collected for 3 days, strays spreading garbage on the street.', 'Waste management', 'Waste Management', 'Bhosari', 'Ward 8 (Bhosari Gaonthan)', 'Weekly Bazaar Ground', 'HIGH', 'Assigned', 'Suresh Shinde', '9822087654', 3, 'https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=600', DATE_ADD(NOW(), INTERVAL 12 HOUR), false, DATE_SUB(NOW(), INTERVAL 16 HOUR)),
(4, 'GV-1004', 'Non-functional streetlights on Kunal Icon Road', 'Entire stretch of 8 streetlights dark for past two weeks posing safety hazard for pedestrians.', 'Streetlights', 'Electrical Department', 'Pimple Saudagar', 'Ward 31 (Pimple Saudagar)', 'Opposite Roseland Residency', 'MEDIUM', 'Submitted', 'Anjali Kulkarni', '9822076543', NULL, NULL, DATE_ADD(NOW(), INTERVAL 22 HOUR), false, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(5, 'GV-1005', 'Open manhole cover on footpath near bus stop', 'Dangerous missing cement cover on stormwater drain, children waiting for school bus.', 'Drainage and sanitation', 'Drainage Department', 'Chinchwad', 'Ward 19 (Chinchwad Station)', 'Near Elpro City Square Mall', 'CRITICAL', 'Resolution Submitted', 'Vipin More', '9822065432', 2, 'https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=600', DATE_ADD(NOW(), INTERVAL 6 HOUR), false, DATE_SUB(NOW(), INTERVAL 36 HOUR)),
(6, 'GV-1006', 'Fallen tree branch blocking walkway in Durga Tekdi Garden', 'Heavy rain branch fell over children playground pathway.', 'Public gardens', 'Garden Department', 'Nigdi', 'Ward 12 (Pradhikaran)', 'Durga Tekdi Sector 26', 'LOW', 'Resolved', 'Kavita Naik', '9822054321', 3, 'https://images.unsplash.com/photo-1448375240586-882707db888b?w=600', DATE_SUB(NOW(), INTERVAL 12 HOUR), false, DATE_SUB(NOW(), INTERVAL 72 HOUR)),
(7, 'GV-1007', 'Contaminated muddy tap water in Sector 21', 'Yellowish water with unpleasant odor received during morning supply slot.', 'Water supply', 'Water Supply & Quality', 'Nigdi', 'Ward 11 (Yamunanagar)', 'Sector 21 Near Water Tank', 'CRITICAL', 'Escalated', 'Mahesh Bansal', '9822043210', 2, NULL, DATE_SUB(NOW(), INTERVAL 18 HOUR), true, DATE_SUB(NOW(), INTERVAL 60 HOUR)),
(8, 'GV-1008', 'Multiple potholes after monsoon showers on BRTS corridor', 'Asphalt washed away causing vehicular bottleneck during peak morning hours.', 'Roads and potholes', 'Roads and Infrastructure', 'Ravet', 'Ward 16 (Ravet-Kiwale)', 'Near D.Y. Patil College Campus', 'HIGH', 'Under Review', 'Sachin Ghorpade', '9822032109', NULL, 'https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600', DATE_ADD(NOW(), INTERVAL 14 HOUR), false, DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(9, 'GV-1009', 'Missed door-to-door garbage collection vehicle', 'Waste collection van did not arrive at our society street today.', 'Waste management', 'Waste Management', 'Sangvi', 'Ward 33 (Old Sangvi)', 'Ganga Nagar Lane 4', 'MEDIUM', 'Submitted', 'Meena Jagtap', '9822021098', NULL, NULL, DATE_ADD(NOW(), INTERVAL 20 HOUR), false, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(10, 'GV-1010', 'Clogged drainage causing waterlogging on service road', 'Sewage overflow onto pedestrian walkway near highway underpass.', 'Drainage and sanitation', 'Drainage Department', 'Kasarwadi', 'Ward 22 (Kasarwadi-Phugewadi)', 'Highway Underpass Pillar 12', 'HIGH', 'Closed', 'Rajesh Gaikwad', '9822010987', 2, NULL, DATE_SUB(NOW(), INTERVAL 48 HOUR), false, DATE_SUB(NOW(), INTERVAL 96 HOUR));

-- 6. Seed Evidence Records for Resolved and In-Progress Cases
INSERT INTO evidence_records (id, complaint_id, officer_id, evidence_type, file_name, file_path, file_size, mime_type, notes, created_at) VALUES
(1, 5, 2, 'BEFORE_PHOTO', 'manhole_before_repair.jpg', 'https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=600', 245000, 'image/jpeg', 'Site inspection confirmed missing pre-cast cover. Barricaded immediately.', DATE_SUB(NOW(), INTERVAL 30 HOUR)),
(2, 5, 2, 'AFTER_PHOTO', 'manhole_fixed_reinforced.jpg', 'https://images.unsplash.com/photo-1584467735871-8e85353a8413?w=600', 312000, 'image/jpeg', 'New heavy-duty SFRC cover installed and leveled with asphalt footpath.', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(3, 1, 2, 'INSPECTION_REPORT', 'site_inspection_measurements.pdf', '/uploads/demo_inspection.pdf', 120400, 'application/pdf', 'Site measured: 1.8m x 1.2m depth 12cm. Cold mix asphalt batch requisitioned.', DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- 7. Seed Escalations
INSERT INTO escalations (id, complaint_id, escalation_level, reason, triggered_by, target_role, is_resolved, created_at) VALUES
(1, 2, 1, 'Standard 48-hour deadline breached without resolution submission.', 'SYSTEM_DEADLINE', 'SUPERVISOR', false, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 7, 2, 'Contaminated drinking water unaddressed past critical 24-hour SLA. Escalated directly to Executive Engineer.', 'SUPERVISOR_MANUAL', 'SUPERVISOR', false, DATE_SUB(NOW(), INTERVAL 18 HOUR));

-- 8. Seed Audit Log Events
INSERT INTO audit_events (actor_username, actor_role, complaint_id, ticket_id, action, previous_state, requested_state, outcome, reason, client_ip, timestamp) VALUES
('citizen_rahul', 'CITIZEN', 1, 'GV-1001', 'SUBMIT_COMPLAINT', NULL, 'Submitted', 'SUCCESS', 'New pothole grievance reported via PCMC Citizen Portal', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 24 HOUR)),
('officer_sharma', 'OFFICER', 1, 'GV-1001', 'TRANSITION', 'Submitted', 'Under Review', 'SUCCESS', 'Officer took complaint for technical validation', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 22 HOUR)),
('supervisor_kulkarni', 'SUPERVISOR', 1, 'GV-1001', 'ASSIGN_OFFICER', 'Under Review', 'Assigned', 'SUCCESS', 'Assigned to field engineer Amit Sharma', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 20 HOUR)),
('officer_sharma', 'OFFICER', 1, 'GV-1001', 'TRANSITION', 'Assigned', 'Inspection Required', 'SUCCESS', 'Site inspection initiated', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 18 HOUR)),
('officer_sharma', 'OFFICER', 1, 'GV-1001', 'TRANSITION_ATTEMPT', 'Inspection Required', 'Resolved', 'REJECTED', 'Cannot skip In Progress and missing completion evidence photos', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 16 HOUR)),
('system_escalation', 'SYSTEM', 2, 'GV-1002', 'DEADLINE_ESCALATION', 'In Progress', 'In Progress', 'SUCCESS', 'Simulated 48-hour SLA expired. Automated escalation triggered to Supervisor.', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- 9. Seed AI Recommendations
INSERT INTO ai_recommendations (id, complaint_id, suggested_category, suggested_department, suggested_priority, confidence_score, explanation, status, created_at) VALUES
(1, 1, 'Roads and potholes', 'Roads and Infrastructure', 'HIGH', 0.94, 'Identified road hazard keyword "deep crater pothole" and station vicinity risk.', 'APPROVED', DATE_SUB(NOW(), INTERVAL 24 HOUR)),
(2, 4, 'Streetlights', 'Electrical Department', 'MEDIUM', 0.91, 'Identified public lighting failure keywords "non-functional streetlights".', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 9, 'Waste management', 'Waste Management', 'MEDIUM', 0.96, 'Identified municipal sanitation keywords "door-to-door garbage collection vehicle".', 'PENDING', DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- 10. Seed Garbage Collection Vans (Live Tracking Feature in PCMC)
INSERT INTO garbage_vans (id, vehicle_number, driver_name, contact_phone, assigned_zone, current_latitude, current_longitude, heading, speed_kmph, status, last_updated_at) VALUES
(1, 'MH-14-GH-1001', 'Santosh Pawar', '9822112233', 'Zone A: Nigdi - Pradhikaran', 18.6540, 73.7715, 65.0, 19.5, 'ON_DUTY', NOW()),
(2, 'MH-14-GH-1002', 'Ganesh Kadam', '9822114455', 'Zone B: Wakad - Hinjawadi', 18.5992, 73.7634, 110.0, 16.0, 'ON_DUTY', NOW()),
(3, 'MH-14-GH-1003', 'Ramesh Gaikwad', '9822116677', 'Zone C: Bhosari - MIDC', 18.6278, 73.8441, 45.0, 22.0, 'ON_DUTY', NOW());

-- 11. Seed Route Stops with Real PCMC Locality Coordinates
INSERT INTO garbage_van_stops (van_id, locality_name, stop_order, latitude, longitude, scheduled_arrival_time, is_completed, estimated_arrival_minutes) VALUES
(1, 'Sector 24 Pradhikaran', 1, 18.6480, 73.7650, '07:30 AM', true, 0),
(1, 'Akurdi Railway Station Chowk', 2, 18.6515, 73.7680, '08:00 AM', true, 0),
(1, 'Bhakti Shakti Chowk, Nigdi', 3, 18.6540, 73.7715, '08:30 AM', false, 4),
(1, 'Yamunanagar Sector 21', 4, 18.6600, 73.7760, '09:00 AM', false, 16),
(1, 'Rupeenagar Main Chowk', 5, 18.6680, 73.7820, '09:30 AM', false, 28),

(2, 'Dutta Mandir Chowk, Wakad', 1, 18.5970, 73.7600, '07:45 AM', true, 0),
(2, 'Kaspate Vasti, Wakad', 2, 18.5992, 73.7634, '08:15 AM', false, 5),
(2, 'Bhumkar Chowk', 3, 18.6025, 73.7550, '08:45 AM', false, 18),
(2, 'Hinjawadi Phase 1 Shivaji Chowk', 4, 18.5912, 73.7389, '09:15 AM', false, 32),

(3, 'Bhosari Dighi Road', 1, 18.6250, 73.8400, '08:00 AM', true, 0),
(3, 'Bhosari Gaonthan Bazaar', 2, 18.6278, 73.8441, '08:30 AM', false, 3),
(3, 'MIDC Bhosari Block J', 3, 18.6320, 73.8490, '09:00 AM', false, 15),
(3, 'Indrayani Nagar Sector 1', 4, 18.6380, 73.8550, '09:30 AM', false, 25);
