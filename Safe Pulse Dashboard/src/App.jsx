import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LocationTracker from './components/LocationTracker';
import PrivacyPolicy from './components/PrivacyPolicy/PrivacyPolicy';
import AccountManage from './components/Account/Accountmanage';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<LocationTracker />} />
          <Route path="/privacy-policy" element={<PrivacyPolicy />} />
          <Route path="/account-deletion" element={<AccountManage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;