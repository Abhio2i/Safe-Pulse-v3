// import React, { useEffect } from 'react';
// import './PrivacyPolicy.css';

// const PrivacyPolicy = () => {
//   useEffect(() => {
//     const handleScroll = () => {
//       const sections = document.querySelectorAll('.policy-section');
//       sections.forEach(section => {
//         const sectionTop = section.getBoundingClientRect().top;
//         const windowHeight = window.innerHeight;
//         if (sectionTop < windowHeight * 0.75) {
//           section.style.opacity = '1';
//           section.style.transform = 'translateY(0)';
//         }
//       });
//     };

//     window.addEventListener('scroll', handleScroll);
//     handleScroll(); // Trigger on initial load

//     return () => window.removeEventListener('scroll', handleScroll);
//   }, []);

//   return (
//     <div className="privacy-policy-container">
//       <div className="policy-header">
//         <div className="header-content">
//           <h1 className="title-animate">Privacy Policy for <span className="app-name">Safe Pulse</span></h1>
//           <div className="header-meta">
//             <p className="effective-date pulse">Effective Date: 2025-06-05</p>
//             <p className="last-updated pulse">Last Updated: 2025-06-05</p>
//           </div>
//         </div>
//         <div className="header-decoration">
//           <div className="circle circle-1"></div>
//           <div className="circle circle-2"></div>
//           <div className="circle circle-3"></div>
//         </div>
//       </div>

//       <section className="intro-section">
//         <div className="intro-content">
//           <p className="intro-text">
//             Thank you for using Dr.Skin . Your privacy is important to us. 
//             This Privacy Policy explains how we collect, use, and protect your personal information 
//             when you use our app.
//           </p>
//           <div className="trust-badge">
//             <div className="shield-icon">🛡️</div>
//             <span className="trust-note">Your Data is Protected</span>
//           </div>
//         </div>
//       </section>

//       <section className="policy-section">
//         <h2 className="section-title">
//           <span className="title-number">1.</span>
//           <span className="title-text">Information We Collect</span>
//         </h2>
//         <p className="section-intro">We collect the following types of information:</p>
        
//         <div className="info-card">
//           <h3 className="card-title">a. Personal Information</h3>
//           <ul className="card-list">
//             <li>Name, email, phone number (for registration and account purposes)</li>
//             <li>Profile details (age, gender, etc., if provided)</li>
//             <li>Appointment details and chat messages (for consultation history)</li>
//           </ul>
//         </div>
        
//         <div className="info-card">
//           <h3 className="card-title">b. Health and Medical Data</h3>
//           <ul className="card-list">
//             <li>Images of skin infections uploaded by the user</li>
//             <li>AI-generated diagnosis and consultation details</li>
//             <li className="highlight-note">Note: These images and health data are only stored temporarily and securely for diagnosis and medical consultation purposes.</li>
//           </ul>
//         </div>
        
//         <div className="info-card">
//           <h3 className="card-title">c. Device and Log Information</h3>
//           <ul className="card-list">
//             <li>Device type, OS version, app version</li>
//             <li>IP address, time zone, and crash logs</li>
//             <li>Usage data (e.g., screen time, button clicks)</li>
//           </ul>
//         </div>
        
//         <div className="info-card">
//           <h3 className="card-title">d. Camera and Storage Access</h3>
//           <ul className="card-list">
//             <li>Used only for taking/uploading skin infection images</li>
//             <li>We do not access or collect any media outside the app</li>
//           </ul>
//         </div>
//       </section>

//       {/* Other sections follow the same pattern */}

//       <section className="policy-section">
//         <h2 className="section-title">
//           <span className="title-number">10.</span>
//           <span className="title-text">Contact Us</span>
//         </h2>
//         <div className="contact-card">
//           <div className="contact-icon">📧</div>
//           <div className="contact-details">
//             <p>If you have any questions or concerns, please contact:</p>
//             <address>
//               <strong>Oxygen To Innovation</strong><br />
//               <a href="mailto:support@oxygen2innovation.com" className="contact-link">
//                 support@oxygen2innovation.com
//               </a><br />
//               <span className="location">📍 B-129, B Block, Sector 6, Noida, Uttar Pradesh 201301, India</span>
//             </address>
//           </div>
//         </div>
//         <div className="trust-message">
//           <div className="lock-icon">🔒</div>
//           <p className="trust-text">
//             Your privacy and trust are our top priorities. We are committed to keeping your personal and medical information safe.
//           </p>
//         </div>
//       </section>

//       <div className="floating-shapes">
//         <div className="shape shape-1"></div>
//         <div className="shape shape-2"></div>
//         <div className="shape shape-3"></div>
//       </div>
//     </div>
//   );
// };

// export default PrivacyPolicy;




import React, { useEffect } from 'react';
import './PrivacyPolicy.css';

const PrivacyPolicy = () => {
  useEffect(() => {
    const handleScroll = () => {
      const sections = document.querySelectorAll('.policy-section');
      sections.forEach(section => {
        const sectionTop = section.getBoundingClientRect().top;
        const windowHeight = window.innerHeight;
        if (sectionTop < windowHeight * 0.75) {
          section.style.opacity = '1';
          section.style.transform = 'translateY(0)';
        }
      });
    };

    window.addEventListener('scroll', handleScroll);
    handleScroll(); // Trigger on initial load

    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <div className="privacy-policy-container">
      <div className="policy-header">
        <div className="header-content">
          <h1 className="title-animate">Privacy Policy for <span className="app-name">Safe Pulse</span></h1>
          <div className="header-meta">
            <p className="effective-date pulse">Effective Date: 2025-06-05</p>
            <p className="last-updated pulse">Last Updated: 2025-06-05</p>
          </div>
          <div className="developer-badge">
            <span>Developed by </span>
            <span className="developer-name">Oxygen 2 Innovation</span>
          </div>
        </div>
        <div className="header-decoration">
          <div className="circle circle-1"></div>
          <div className="circle circle-2"></div>
          <div className="circle circle-3"></div>
        </div>
      </div>

      <section className="intro-section">
        <div className="intro-content">
          <p className="intro-text">
            Thank you for using FamilyGuard Tracker, developed by Oxygen 2 Innovation. Your family's safety and privacy are our top priorities. 
            This Privacy Policy explains how we collect, use, and protect location and personal information 
            through our advanced real-time family tracking solution.
          </p>
          <div className="trust-badge">
            <div className="shield-icon">🛡️</div>
            <span className="trust-note">Powered by Oxygen 2 Innovation's Secure Technology</span>
          </div>
        </div>
      </section>

      <section className="policy-section">
        <h2 className="section-title">
          <span className="title-number">1.</span>
          <span className="title-text">Information We Collect</span>
        </h2>
        <p className="section-intro">Oxygen 2 Innovation's tracking technology collects:</p>
        
        <div className="info-card">
          <h3 className="card-title">a. Advanced Location Data</h3>
          <ul className="card-list">
            <li>Precise real-time GPS coordinates with Oxygen 2 Innovation's proprietary tracking algorithm</li>
            <li>Intelligent location history with movement analytics</li>
            <li>Geofence monitoring with multi-zone support</li>
            <li className="highlight-note">Oxygen 2 Innovation's technology ensures continuous tracking, auto-restart after reboot, and offline recovery</li>
          </ul>
        </div>
        
        <div className="info-card">
          <h3 className="card-title">b. Family Network Information</h3>
          <ul className="card-list">
            <li>Family member profiles with customizable permissions</li>
            <li>Emergency contact networks with priority levels</li>
            <li>Trusted places database (home, school, etc.)</li>
          </ul>
        </div>
        
        <div className="info-card">
          <h3 className="card-title">c. Device Optimization Data</h3>
          <ul className="card-list">
            <li>Battery efficiency metrics (Oxygen 2 Innovation's low-power tracking mode)</li>
            <li>Network connectivity status for seamless transitions</li>
            <li>Device health monitoring for reliable tracking</li>
          </ul>
        </div>
      </section>

      <section className="policy-section">
        <h2 className="section-title">
          <span className="title-number">2.</span>
          <span className="title-text">Oxygen 2 Innovation's Technology</span>
        </h2>
        
        <div className="info-card">
          <h3 className="card-title">Reliable Tracking Features</h3>
          <ul className="card-list">
            <li><strong>Always-On Tracking:</strong> Our patented technology maintains location updates even during network interruptions</li>
            <li><strong>Auto-Recovery System:</strong> Automatically restarts tracking after device reboot</li>
            <li><strong>Smart Battery Management:</strong> Optimizes tracking frequency based on battery level</li>
          </ul>
        </div>
        
        <div className="info-card">
          <h3 className="card-title">Safety Innovations</h3>
          <ul className="card-list">
            <li><strong>Emergency SOS:</strong> Instant alerts with location mapping to all designated contacts</li>
            <li><strong>Geofence Intelligence:</strong> Custom zones with entry/exit notifications</li>
            <li><strong>Offline Protection:</strong> Stores and transmits location data when connection is restored</li>
          </ul>
        </div>
      </section>

      <section className="policy-section">
        <h2 className="section-title">
          <span className="title-number">3.</span>
          <span className="title-text">Data Security by Oxygen 2 Innovation</span>
        </h2>
        
        <div className="info-card">
          <h3 className="card-title">Our Protection Promise</h3>
          <ul className="card-list">
            <li>Military-grade encryption for all location data transmissions</li>
            <li>Secure cloud infrastructure with regular penetration testing</li>
            <li>Strict access controls and audit logging</li>
            <li>Automatic data purging policies</li>
          </ul>
        </div>
        
        <div className="info-card">
          <h3 className="card-title">Compliance Standards</h3>
          <ul className="card-list">
            <li>GDPR-compliant data practices</li>
            <li>COPPA-certified children's privacy protection</li>
            <li>Regular third-party security audits</li>
          </ul>
        </div>
      </section>

      <section className="policy-section">
        <h2 className="section-title">
          <span className="title-number">4.</span>
          <span className="title-text">Family Safety Controls</span>
        </h2>
        
        <div className="info-card">
          <h3 className="card-title">Privacy Management</h3>
          <ul className="card-list">
            <li>Granular sharing permissions for each family member</li>
            <li>Temporary location sharing options</li>
            <li>Activity history review with timeline</li>
          </ul>
        </div>
        
        <div className="info-card">
          <h3 className="card-title">Emergency Features</h3>
          <ul className="card-list">
            <li>One-tap SOS with automatic location updates</li>
            <li>Discreet alert modes for dangerous situations</li>
            <li>Automatic emergency services notification</li>
          </ul>
        </div>
      </section>

      <section className="policy-section">
        <h2 className="section-title">
          <span className="title-number">5.</span>
          <span className="title-text">Contact Oxygen 2 Innovation</span>
        </h2>
        <div className="contact-card oxygen-contact">
          <div className="company-logo"></div>
          <div className="contact-details">
            <h3>Oxygen 2 Innovation Support</h3>
            <address>
              <strong>Registered Office:</strong><br />
              B-129, B Block, Sector 6, Noida, Uttar Pradesh 201301, India<br />
              
              <strong>Technology Support:</strong><br />
              <a href="mailto:familyguard-support@oxygen2innovation.com" className="contact-link">
                familyguard-support@oxygen2innovation.com
              </a><br />
              
              <strong>Emergency Support:</strong><br />
              <span className="emergency-contact">+91 120 4152 369 (24/7)</span>
            </address>
          </div>
        </div>
        <div className="trust-message">
          <div className="lock-icon">🔒</div>
          <p className="trust-text">
            Oxygen 2 Innovation is committed to building technology that protects your family while respecting your privacy. 
            Our secure infrastructure and innovative tracking solutions provide peace of mind for modern families.
          </p>
        </div>
      </section>

      <div className="floating-shapes">
        <div className="shape shape-1"></div>
        <div className="shape shape-2"></div>
        <div className="shape shape-3"></div>
      </div>
    </div>
  );
};

export default PrivacyPolicy;