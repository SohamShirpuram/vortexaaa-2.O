/**
 * GovFix — Bilingual Engine (English & मराठी / Marathi)
 * Provides instant language toggling for government-grade accessibility
 */

const GovFixI18n = {
  currentLang: localStorage.getItem('govfix_lang') || 'en',

  translations: {
    en: {
      site_title: "GovFix — Smart Civic Services for Pimpri-Chinchwad",
      banner_official: "An Independent Municipal Civic Prototype for Pimpri-Chinchwad Municipal Area",
      banner_how: "Here's how you know",
      nav_home: "Home",
      nav_services: "Citizen Services",
      nav_garbage: "Garbage Tracking",
      nav_grievances: "Grievances",
      nav_track: "Track Ticket",
      nav_directory: "Departments",
      nav_dashboard: "Dashboard",
      nav_about: "About",
      nav_contact: "Contact & Help",
      hero_pill: "Municipal Innovation Prototype • PCMC Inspiration",
      hero_title: "Your City. Your Services. One Smarter Platform.",
      hero_desc: "Report civic issues, track municipal services and stay informed about your locality across Pimpri-Chinchwad.",
      search_placeholder: "Search all civic services (e.g. property tax, water bill, pothole, garbage van)...",
      search_btn: "Search",
      btn_report_issue: "Report a Civic Issue",
      btn_track_issue: "Track a Complaint",
      btn_track_van: "Track Garbage Collection",
      sec_quick_services: "Explore Municipal Services",
      sec_quick_services_desc: "Direct access to Pimpri-Chinchwad civic amenities and municipal grievance redressing",
      srv_tax_title: "Property Tax (Kar)",
      srv_tax_desc: "Self-assessment, tax rebate lookup and official PCMC payment portal redirection.",
      srv_water_title: "Water Supply (Pani Puravatha)",
      srv_water_desc: "Pipeline leakage reporting, contaminated water grievances and supply schedules.",
      srv_roads_title: "Roads & Pothole Repair",
      srv_roads_desc: "Report asphalt craters, dangerous road depressions, broken dividers or BRTS corridor issues.",
      srv_waste_title: "Solid Waste Management",
      srv_waste_desc: "Door-to-door collection grievances, overflowing bin alerts and live vehicle GPS monitoring.",
      srv_drainage_title: "Drainage & Sanitation",
      srv_drainage_desc: "Open manhole hazards, stormwater drainage choking and sewer overflow reporting.",
      srv_lights_title: "Streetlight Maintenance",
      srv_lights_desc: "Dark street safety hazards, damaged electric poles and non-functional LED fixtures.",
      srv_permits_title: "Building & BPMS Approvals",
      srv_permits_desc: "Town planning layouts, occupancy certificates and building permission verification.",
      srv_gardens_title: "Gardens & Tree Trimming",
      srv_gardens_desc: "Fallen tree branch clearing, municipal park maintenance and playground repairs.",
      stat_registered: "Complaints Registered",
      stat_in_progress: "Cases In Progress",
      stat_resolved: "Resolved with Evidence",
      stat_overdue: "Overdue SLAs",
      stat_vans: "Active Garbage Vans",
      step1_title: "1. Report an Issue",
      step1_desc: "Submit your grievance with locality details and optional photos via web.",
      step2_title: "2. Automated Assignment",
      step2_desc: "Routed instantly to the responsible junior engineer and ward office.",
      step3_title: "3. Track with Evidence",
      step3_desc: "Monitor progress timeline with mandatory inspection reports and completion photos.",
      step4_title: "4. Review & Confirm",
      step4_desc: "Verify quality resolution, rate service, or request reopening if unsatisfied."
    },
    mr: {
      site_title: "GovFix — पिंपरी-चिंचवडसाठी स्मार्ट नागरी सेवा",
      banner_official: "पिंपरी-चिंचवड महानगरपालिका परिसरासाठी स्वतंत्र नागरी सेवा प्रोटोटाइप",
      banner_how: "अधिक माहिती",
      nav_home: "मुख्यपृष्ठ",
      nav_services: "नागरी सेवा",
      nav_garbage: "कचरा गाडी ट्रॅकिंग",
      nav_grievances: "तक्रारी",
      nav_track: "अर्जाची स्थिती",
      nav_directory: "विभाग निर्देशिका",
      nav_dashboard: "डॅशबोर्ड",
      nav_about: "माहिती",
      nav_contact: "संपर्क व मदत",
      hero_pill: "महानगरपालिका नवोपक्रम • PCMC प्रेरणा",
      hero_title: "आपले शहर. आपल्या सेवा. एक स्मार्ट व्यासपीठ.",
      hero_desc: "पिंपरी-चिंचवडमधील नागरी समस्या नोंदवा, सेवा ट्रॅक करा आणि आपल्या परिसराबाबत जागरूक राहा.",
      search_placeholder: "सर्व नागरी सेवा शोधा (उदा. मालमत्ता कर, पाणी बिल, खड्डा तक्रार, कचरा गाडी)...",
      search_btn: "शोधा",
      btn_report_issue: "नागरी समस्या नोंदवा",
      btn_track_issue: "तक्रारीची स्थिती पाहा",
      btn_track_van: "कचरा गाडी ट्रॅक करा",
      sec_quick_services: "महानगरपालिका सेवा एक्सप्लोर करा",
      sec_quick_services_desc: "पिंपरी-चिंचवड नागरी सुविधा आणि तक्रार निवारण थेट उपलब्ध",
      srv_tax_title: "मालमत्ता कर (घरपट्टी)",
      srv_tax_desc: "कर आकारणी, सवलत माहिती आणि अधिकृत PCMC पोर्टल पुनर्निर्देशन.",
      srv_water_title: "पाणी पुरवठा सेवा",
      srv_water_desc: "पाईपलाईन गळती, दूषित पाणी तक्रार आणि पाणी सोडण्याचे वेळापत्रक.",
      srv_roads_title: "रस्ते आणि खड्डे दुरुस्ती",
      srv_roads_desc: "रस्त्यांवरील खड्डे, दुभाजक नुकसान किंवा बीआरटीएस मार्गावरील समस्यांची नोंद करा.",
      srv_waste_title: "घनकचरा व्यवस्थापन",
      srv_waste_desc: "घरोघरी कचरा संकलन तक्रार, कचराकुंडी स्वच्छता आणि थेट जीपीएस ट्रॅकिंग.",
      srv_drainage_title: "ड्रेनेज व स्वच्छता",
      srv_drainage_desc: "उघडी मॅनहोल झाकणे, पावसाळी गटार तुंबणे आणि सांडपाणी समस्यांची नोंद.",
      srv_lights_title: "पथदिवे देखभाल",
      srv_lights_desc: "बंद पथदिवे, झुकलेले विद्युत खांब आणि सुरक्षिततेबाबत तक्रारी.",
      srv_permits_title: "बांधकाम परवाने (BPMS)",
      srv_permits_desc: "नगररचना, भोगवटा प्रमाणपत्र आणि बांधकाम परवानगी पडताळणी.",
      srv_gardens_title: "उद्याने व वृक्ष छाटणी",
      srv_gardens_desc: "पडलेली झाडे हटवणे, उद्यान देखभाल आणि खेळाच्या मैदानांची दुरुस्ती.",
      stat_registered: "नोंदवलेल्या तक्रारी",
      stat_in_progress: "प्रगतीपथावरील प्रकरणे",
      stat_resolved: "पुरावा पडताळून सोडवलेले",
      stat_overdue: "मुदत संपलेली प्रकरणे",
      stat_vans: "कार्यरत कचरा गाड्या",
      step1_title: "१. समस्या नोंदवा",
      step1_desc: "परिसराचा पत्ता आणि फोटोसह तुमची तक्रार सहज नोंदवा.",
      step2_title: "२. स्वयंचलित वाटप",
      step2_desc: "संबंधित प्रभागातील कनिष्ठ अभियंता व विभागाकडे थेट वर्ग.",
      step3_title: "३. पुराव्यासह ट्रॅक करा",
      step3_desc: "प्रत्यक्ष कामाची पाहणी आणि काम पूर्ण झाल्याचे फोटो तपासा.",
      step4_title: "४. पुनरावलोकन व समाधान",
      step4_desc: "कामाचे मूल्यमापन करा किंवा असमाधानी असल्यास पुन्हा उघडण्याची विनंती करा."
    }
  },

  init() {
    this.applyLanguage(this.currentLang);
    this.bindEvents();
  },

  toggleLanguage() {
    this.currentLang = this.currentLang === 'en' ? 'mr' : 'en';
    localStorage.setItem('govfix_lang', this.currentLang);
    this.applyLanguage(this.currentLang);
  },

  applyLanguage(lang) {
    const dict = this.translations[lang] || this.translations.en;
    document.querySelectorAll('[data-i18n]').forEach(el => {
      const key = el.getAttribute('data-i18n');
      if (dict[key]) {
        if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
          el.placeholder = dict[key];
        } else {
          el.textContent = dict[key];
        }
      }
    });

    const langToggleBtn = document.getElementById('lang-toggle-btn');
    if (langToggleBtn) {
      langToggleBtn.textContent = lang === 'en' ? 'मराठी (MR)' : 'English (EN)';
    }

    document.documentElement.lang = lang;
  },

  bindEvents() {
    const langBtn = document.getElementById('lang-toggle-btn');
    if (langBtn) {
      langBtn.addEventListener('click', () => this.toggleLanguage());
    }
  }
};

window.GovFixI18n = GovFixI18n;

