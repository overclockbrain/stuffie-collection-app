import { useState, useEffect } from 'react';
import LoginPage from './pages/LoginPage';
import StuffedAnimalPage from './pages/StuffedAnimalPage';
import { SESSION_ENDED_EVENT, type SessionEndedDetail } from './api/client';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('accessToken'));
  // トークン切れで自動ログアウトしたときだけログイン画面にメッセージを出す
  const [sessionExpired, setSessionExpired] = useState(false);

  useEffect(() => {
    const handleSessionEnded = (e: Event) => {
      const detail = (e as CustomEvent<SessionEndedDetail>).detail;
      setIsLoggedIn(false);
      setSessionExpired(detail?.reason === 'expired');
    };

    window.addEventListener(SESSION_ENDED_EVENT, handleSessionEnded);
    return () => window.removeEventListener(SESSION_ENDED_EVENT, handleSessionEnded);
  }, []);

  const handleLoginSuccess = () => {
    setSessionExpired(false);
    setIsLoggedIn(true);
  };

  if (!isLoggedIn) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} sessionExpired={sessionExpired} />;
  }

  return <StuffedAnimalPage />;
}

export default App;