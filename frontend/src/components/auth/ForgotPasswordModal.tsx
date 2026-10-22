import React, { useState } from 'react';
import { AlertCircle, CheckCircle, Mail, X } from 'lucide-react';
import { fetchDevEmails, forgotPassword } from '../../services/api';

interface ForgotPasswordModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOpenLogin: () => void;
}

export const ForgotPasswordModal: React.FC<ForgotPasswordModalProps> = ({
  isOpen,
  onClose,
  onOpenLogin,
}) => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [resetUrl, setResetUrl] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!email.trim()) {
      setError('Please provide your email address.');
      return;
    }

    setLoading(true);
    try {
      await forgotPassword(email.trim());
      setSuccess(true);

      // Check dev outbox for reset link
      try {
        const emails = await fetchDevEmails();
        const found = emails.find(
          m => m.type === 'PASSWORD_RESET' && m.to.toLowerCase() === email.trim().toLowerCase()
        );
        if (found?.actionUrl) {
          setResetUrl(found.actionUrl);
        }
      } catch {
        // ignore
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to request password reset.');
    } finally {
      setLoading(false);
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
          <h2 className="text-2xl font-bold text-white tracking-tight">Reset Password</h2>
          <p className="text-sm text-muted mt-1">We'll send a password reset link to your email</p>
        </div>

        {error && (
          <div className="mb-4 p-3.5 bg-red-500/10 border border-red-500/20 rounded-xl flex items-start gap-3 text-red-400 text-sm">
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
            <p>{error}</p>
          </div>
        )}

        {success ? (
          <div className="text-center py-2">
            <div className="w-12 h-12 bg-green-500/10 text-green-400 rounded-xl flex items-center justify-center mx-auto mb-3 border border-green-500/20">
              <CheckCircle className="w-6 h-6" />
            </div>
            <p className="text-sm text-gray-200 mb-4">
              If an account with <span className="text-white font-semibold">{email}</span> exists, a password reset link has been dispatched.
            </p>

            {resetUrl && (
              <div className="p-3 bg-primary/10 border border-primary/20 rounded-xl mb-4 text-left">
                <p className="text-xs font-semibold text-primary mb-1">Demo Quick Action:</p>
                <a
                  href={resetUrl}
                  onClick={() => onClose()}
                  className="inline-block py-2 px-3 bg-primary text-white text-xs font-medium rounded-lg hover:bg-blue-600 transition-colors"
                >
                  Open Password Reset Page
                </a>
              </div>
            )}

            <button
              onClick={() => {
                setSuccess(false);
                onClose();
                onOpenLogin();
              }}
              className="w-full py-2.5 bg-surface hover:bg-white/5 border border-white/10 text-white rounded-xl text-sm font-medium transition-all"
            >
              Back to Sign In
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-gray-300 mb-1.5">
                Registered Email Address <span className="text-red-400">*</span>
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

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-primary hover:bg-blue-600 disabled:opacity-50 text-white font-medium rounded-xl text-sm transition-all shadow-lg shadow-primary/20 flex items-center justify-center gap-2"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                'Send Reset Link'
              )}
            </button>

            <div className="text-center pt-2">
              <button
                type="button"
                onClick={() => {
                  onClose();
                  onOpenLogin();
                }}
                className="text-xs text-muted hover:text-white transition-colors"
              >
                Cancel and return to sign in
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
