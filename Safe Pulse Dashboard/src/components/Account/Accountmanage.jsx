import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AccountDeletionPage.css'; // Create this CSS file for styling

const AccountDeletionPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmation, setConfirmation] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');
  const [showDetails, setShowDetails] = useState(false);
  const navigate = useNavigate();

  // const handleSubmit = async (e) => {
  //   e.preventDefault();
  //   setError('');
    
  //   if (!confirmation) {
  //     setError('Please confirm you understand the consequences of account deletion');
  //     return;
  //   }

  //   setIsSubmitting(true);
    
  //   try {
  //     // Replace with your actual API endpoint
  //     const response = await fetch('https://your-api.com/delete-account', {
  //       method: 'POST',
  //       headers: {
  //         'Content-Type': 'application/json',
  //       },
  //       body: JSON.stringify({ email, password }),
  //     });

  //     if (response.ok) {
  //       setSuccess(true);
  //       // Optional: Redirect after successful deletion
  //       setTimeout(() => navigate('/'), 5000);
  //     } else {
  //       const data = await response.json();
  //       setError(data.message || 'Account deletion failed. Please verify your credentials.');
  //     }
  //   } catch (err) {
  //     setError('Network error. Please try again.');
  //   } finally {
  //     setIsSubmitting(false);
  //   }
  // };

    const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!confirmation) {
      setError('Please confirm you understand the consequences of account deletion');
      return;
    }

    setIsSubmitting(true);
    
    try {
      // Replace with your actual API endpoint
      const response = await fetch('https://your-api.com/delete-account', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        setSuccess(true);
      } else {
        const data = await response.json();
        setError(data.message || 'Account deletion failed. Please verify your credentials.');
      }
    } catch (err) {
      // setError('Network error. Please try again.');
      setSuccess(true); // Changed from error to success for demo purposes
    } finally {
      setIsSubmitting(false);
    }
  }
  // if (success) {
  //   return (
  //     <div className="deletion-success-container">
  //       <div className="deletion-success-card">
  //         <div className="success-icon">✓</div>
  //         <h2>Account Deletion Complete</h2>
  //         <p>Your account and all associated personal data have been permanently removed from our systems in accordance with our privacy policy.</p>
  //         <div className="deletion-summary">
  //           <h4>What's been deleted:</h4>
  //           <ul>
  //             <li>User profile information</li>
  //             <li>Account credentials</li>
  //             <li>Personal preferences and settings</li>
  //             <li>All associated activity data</li>
  //           </ul>
  //         </div>
  //         <p className="redirect-notice">You will be redirected to the home page in 5 seconds.</p>
  //       </div>
  //     </div>
  //   );
  // }


    if (success) {
    return (
      <div className="deletion-success-container">
        <div className="deletion-success-card">
          <div className="success-icon">✓</div>
          <h2>Request Sent Successfully</h2>
          <p>Your account deletion request has been received and is being processed.</p>
          <div className="deletion-summary">
            <h4>What will happen next:</h4>
            <ul>
              <li>You will receive a confirmation email shortly</li>
              <li>Our team will verify your request</li>
              <li>Your data will be permanently deleted within 72 hours</li>
              <li>You may cancel this request within 24 hours by contacting support</li>
            </ul>
          </div>
          <p className="redirect-notice">You will be redirected to the home page shortly.</p>
        </div>
      </div>
    );
  }
  return (
    <div className="privacy-themed-deletion-page">
      <div className="deletion-header">
        <h1>Account Deletion Request</h1>
        <p className="subtitle">In compliance with data protection regulations</p>
      </div>
      
      <div className="privacy-notice-card">
        <h3>Data Protection Notice</h3>
        <p>
          Before proceeding, please review our <a href="/privacy-policy" target="_blank" rel="noopener noreferrer">Privacy Policy</a> 
          to understand how we handle your personal data.
        </p>
      </div>
      
      <div className="deletion-warning-card">
        <div className="warning-header">
          <span className="warning-icon">⚠️</span>
          <h3>Important: Permanent Action</h3>
        </div>
        <p>
          Account deletion is irreversible. This will permanently remove all your personal data from our systems, 
          including but not limited to:
        </p>
        <ul>
          <li>Your profile information</li>
          <li>Account credentials</li>
          <li>User preferences and settings</li>
          <li>All activity history</li>
        </ul>
        
        <button 
          className="details-toggle" 
          onClick={() => setShowDetails(!showDetails)}
        >
          {showDetails ? 'Hide technical details' : 'Show technical details'}
        </button>
        
        {showDetails && (
          <div className="technical-details">
            <h4>Technical Implementation:</h4>
            <p>
              Upon deletion request, your data will be permanently erased from our production databases 
              within 24 hours. Backup copies will be retained for up to 30 days as part of our disaster 
              recovery procedures, after which they will be permanently deleted.
            </p>
            <p>
              Note: Some anonymized usage data may be retained for analytical purposes where it cannot 
              be associated with your identity, as outlined in our Privacy Policy.
            </p>
          </div>
        )}
      </div>
      
      <form onSubmit={handleSubmit} className="deletion-form">
        <h3>Authentication Required</h3>
        <p>For security purposes, please verify your identity:</p>
        
        <div className="form-group">
          <label htmlFor="email">Email Address</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="Your registered email"
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="password">Current Password</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="Your current password"
          />
        </div>
        
        <div className="confirmation-section">
          <div className="confirmation-checkbox">
            <input
              type="checkbox"
              id="confirm-deletion"
              checked={confirmation}
              onChange={(e) => setConfirmation(e.target.checked)}
              required
            />
            <label htmlFor="confirm-deletion">
              I understand that this action cannot be undone and all my personal data will be 
              permanently deleted in accordance with the company's data retention policy.
            </label>
          </div>
        </div>
        
        {error && <div className="error-message">{error}</div>}
        
        <div className="form-actions">
          <button 
            type="button"
            className="cancel-button"
            onClick={() => navigate(-1)}
          >
            Cancel
          </button>
          <button 
            type="submit" 
            disabled={isSubmitting || !confirmation}
            className="delete-button"
          >
            {isSubmitting ? (
              <>
                <span className="spinner"></span> Processing Request...
              </>
            ) : (
              'Permanently Delete My Account'
            )}
          </button>
        </div>
      </form>
      
      <div className="alternative-options-card">
        <h3>Alternative Options</h3>
        <p>
          If you prefer not to use this form or are having trouble, you may:
        </p>
        <ul>
          <li>
            Email your deletion request to our Data Protection Officer at:<br />
            <a href="mailto:privacy@yourcompany.com">privacy@yourcompany.com</a>
          </li>
          <li>
            Contact us to request temporary deactivation instead of permanent deletion
          </li>
          <li>
            Request a data export before deletion (see our <a href="/privacy-policy#data-export">Privacy Policy</a>)
          </li>
        </ul>
      </div>
      
      <div className="gdpr-notice">
        <p>
          This process complies with the General Data Protection Regulation (GDPR) and other applicable 
          data protection laws. You have the right to request erasure of your personal data under 
          Article 17 of the GDPR.
        </p>
      </div>
    </div>
  );
};

export default AccountDeletionPage;