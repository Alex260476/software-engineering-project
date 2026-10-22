import React, { useState } from 'react';
import { AlertCircle, CheckCircle, Eye, EyeOff, Lock, Mail, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { resendVerificationEmail } from '../../services/api';
import { useNavigate } from 'react-router-dom';

interface LoginModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOpenRegister: () => void;
  onOpenForgotPassword: () => void;
  defaultEmail?: string;
  defaultPassword?: string;
}

export const LoginModal: React.FC<LoginModalProps> = ({
  isOpen,
  onClose,
  onOpenRegister,
  onOpenForgotPassword,
  defaultEmail = '',
  defaultPassword = '',
}) => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState(defaultEmail);
  const [password, setPassword] = useState(defaultPassword);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isUnverified, setIsUnverified] = useState(false);
  const [resendSuccess, setResendSuccess] = useState<string | null>(null);

  // Sync if default props change from Demo Helper
  React.useEffect(() => {
    if (defaultEmail) setEmail(defaultEmail);
    if (defaultPassword) setPassword(defaultPassword);
  }, [defaultEmail, defaultPassword]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsUnverified(false);
    setResendSuccess(null);

    if (!email.trim() || !password) {
      setError('Please provide both email and password.');
      return;
    }

    setLoading(true);
    try {
      const loggedUser = await login({ email: email.trim(), password });
      onClose();
      if (loggedUser.userType === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/');
      }
    } catch (err: any) {
      const errMsg = err?.message || 'Invalid email or password.';
      setError(errMsg);
      if (errMsg.toLowerCase().includes('not verified')) {
        setIsUnverified(true);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email.trim()) return;
    try {
      await resendVerificationEmail(email.trim());
      setResendSuccess('Verification email resent! Check the simulated outbox or your inbox.');
    } catch (err: any) {
      setError(err?.message || 'Failed to resend verification email.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fadeIn">
      <div className="relative w-full max-w-md bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-2xl">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-muted hover:text-white transition-colors"
          aria-label="Close modal"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="mb-6 text-center">
          <h2 className="text-2xl font-bold text-white tracking-tight">Welcome Back</h2>
          <p className="text-sm text-muted mt-1">Sign in to your CinemaFlex account</p>
        </div>

        {error && (
          <div className="mb-4 p-3.5 bg-red-500/10 border border-red-500/20 rounded-xl flex items-start gap-3 text-red-400 text-sm">
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <p className="font-medium">{error}</p>
              {isUnverified && (
                <button
                  type="button"
                  onClick={handleResend}
                  className="mt-2 text-xs font-semibold text-primary underline hover:text-blue-400 block"
                >
                  Resend verification email
                </button>
              )}
            </div>
          </div>
        )}

        {resendSuccess && (
          <div className="mb-4 p-3.5 bg-green-500/10 border border-green-500/20 rounded-xl flex items-center gap-3 text-green-400 text-sm">
            <CheckCircle className="w-5 h-5 flex-shrink-0" />
            <p>{resendSuccess}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-gray-300 mb-1.5">
              Email Address <span className="text-red-400">*</span>
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
              <input
                type="email"
                required
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="name@example.com"
                className="w-full pl-10 pr-4 py-2.5 bg-background border border-white/10 rounded-xl text-white placeholder-muted focus:outline-none focus:border-primary text-sm transition-all"
              />
            </div>
          </div>

          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label className="block text-xs font-medium text-gray-300">
                Password <span className="text-red-400">*</span>
              </label>
              <button
                type="button"
                onClick={() => {
                  onClose();
                  onOpenForgotPassword();
                }}
                className="text-xs text-primary hover:underline font-medium"
              >
                Forgot password?
              </button>
            </div>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
              <input
                type={showPassword ? 'text' : 'password'}
                required
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-10 pr-10 py-2.5 bg-background border border-white/10 rounded-xl text-white placeholder-muted focus:outline-none focus:border-primary text-sm transition-all"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-white"
                tabIndex={-1}
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-primary hover:bg-blue-600 disabled:opacity-50 text-white font-medium rounded-xl text-sm transition-all shadow-lg shadow-primary/20 flex items-center justify-center gap-2 mt-2"
          >
            {loading ? (
              <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              'Sign In'
            )}
          </button>
        </form>

        <div className="mt-6 text-center text-xs text-muted">
          Don't have an account?{' '}
          <button
            onClick={() => {
              onClose();
              onOpenRegister();
            }}
            className="text-primary hover:underline font-semibold ml-1"
          >
            Create an account
          </button>
        </div>
      </div>
    </div>
  );
};
