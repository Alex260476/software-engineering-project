import React, { useEffect, useState } from 'react';
import { useSearchParams, Link, useNavigate } from 'react-router-dom';
import { CheckCircle, AlertCircle, Loader, ArrowRight } from 'lucide-react';
import { verifyEmail } from '../services/api';
import { useAuth } from '../context/AuthContext';

const VerifyEmail: React.FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();
  const { refreshUser } = useAuth();

  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      setError('No verification token provided in the URL.');
      return;
    }

    const verify = async () => {
      try {
        const res = await verifyEmail(token);
        setSuccess(true);
        setMessage(res.message || 'Email verified successfully! Your account is now Active.');
        if (res.token) {
          localStorage.setItem('cinema_token', res.token);
        }
        if (res.user) {
          localStorage.setItem('cinema_user', JSON.stringify(res.user));
          await refreshUser();
        }
      } catch (err: any) {
        setError(err?.message || 'Verification token is invalid or has expired.');
      } finally {
        setLoading(false);
      }
    };

    verify();
  }, [token]);

  return (
    <div className="min-h-[75vh] flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-surface border border-white/10 rounded-2xl p-8 text-center shadow-2xl">
        {loading && (
          <div className="py-8">
            <Loader className="w-12 h-12 text-primary animate-spin mx-auto mb-4" />
            <h2 className="text-xl font-bold text-white mb-2">Verifying Your Account...</h2>
            <p className="text-sm text-muted">Please wait while we confirm your email token.</p>
          </div>
        )}

        {!loading && success && (
          <div className="py-4">
            <div className="w-16 h-16 bg-green-500/10 text-green-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-green-500/20">
              <CheckCircle className="w-10 h-10" />
            </div>
            <h2 className="text-2xl font-bold text-white mb-2">Account Verified!</h2>
            <p className="text-sm text-gray-300 mb-6">{message}</p>
            <div className="p-3 bg-white/5 border border-white/10 rounded-xl text-xs text-muted mb-6">
              Requirement Check: Account status transitioned from <span className="text-amber-400 font-semibold">Inactive</span> to <span className="text-green-400 font-semibold">Active</span>.
            </div>
            <button
              onClick={() => navigate('/')}
              className="w-full py-2.5 px-4 bg-primary hover:bg-blue-600 text-white font-medium rounded-xl text-sm transition-all flex items-center justify-center gap-2"
            >
              Start Browsing Movies <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        )}

        {!loading && error && (
          <div className="py-4">
            <div className="w-16 h-16 bg-red-500/10 text-red-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-red-500/20">
              <AlertCircle className="w-10 h-10" />
            </div>
            <h2 className="text-2xl font-bold text-white mb-2">Verification Failed</h2>
            <p className="text-sm text-red-400 mb-6">{error}</p>
            <Link
              to="/"
              className="inline-block py-2.5 px-6 bg-surface hover:bg-white/5 border border-white/10 text-white rounded-xl text-sm font-medium transition-all"
            >
              Return Home
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default VerifyEmail;
