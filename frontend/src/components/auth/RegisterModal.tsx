import React, { useState } from 'react';
import {
  AlertCircle,
  CheckCircle,
  ChevronDown,
  ChevronUp,
  CreditCard,
  Eye,
  EyeOff,
  Lock,
  Mail,
  MapPin,
  ShieldCheck,
  User,
  X,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { RegistrationData } from '../../types';
import { fetchLatestVerification } from '../../services/api';

interface RegisterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOpenLogin: () => void;
  initialData?: Partial<RegistrationData>;
}

export const RegisterModal: React.FC<RegisterModalProps> = ({
  isOpen,
  onClose,
  onOpenLogin,
  initialData,
}) => {
  const { register } = useAuth();

  const [firstName, setFirstName] = useState(initialData?.firstName || '');
  const [lastName, setLastName] = useState(initialData?.lastName || '');
  const [email, setEmail] = useState(initialData?.email || '');
  const [password, setPassword] = useState(initialData?.password || '');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [promotionSubscription, setPromotionSubscription] = useState(
    initialData?.promotionSubscription !== undefined ? initialData.promotionSubscription : true
  );

  // Optional address & card fields
  const [showOptionalFields, setShowOptionalFields] = useState(false);
  const [street, setStreet] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [zipCode, setZipCode] = useState('');

  const [cardholderName, setCardholderName] = useState('');
  const [cardNumber, setCardNumber] = useState('');
  const [cardType, setCardType] = useState('Visa');
  const [expirationDate, setExpirationDate] = useState('');

  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Success state after registration
  const [registeredEmail, setRegisteredEmail] = useState<string | null>(null);
  const [verificationUrl, setVerificationUrl] = useState<string | null>(null);

  React.useEffect(() => {
    if (initialData) {
      if (initialData.firstName !== undefined) setFirstName(initialData.firstName);
      if (initialData.lastName !== undefined) setLastName(initialData.lastName);
      if (initialData.email !== undefined) setEmail(initialData.email);
      if (initialData.password !== undefined) setPassword(initialData.password);
      if (initialData.promotionSubscription !== undefined)
        setPromotionSubscription(initialData.promotionSubscription);
    }
  }, [initialData]);

  if (!isOpen) return null;

  // Password strength check
  const hasMinLength = password.length >= 8;
  const hasUpper = /[A-Z]/.test(password);
  const hasLower = /[a-z]/.test(password);
  const hasDigit = /\d/.test(password);
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>_]/.test(password);

  const strengthScore = [hasMinLength, hasUpper, hasLower, hasDigit, hasSpecial].filter(Boolean).length;

  const getStrengthColor = () => {
    if (strengthScore <= 2) return 'bg-red-500';
    if (strengthScore <= 4) return 'bg-amber-500';
    return 'bg-green-500';
  };

  const getStrengthLabel = () => {
    if (password.length === 0) return '';
    if (strengthScore <= 2) return 'Weak';
    if (strengthScore <= 4) return 'Moderate';
    return 'Strong';
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (strengthScore < 5) {
      setError('Password must meet all 5 security requirements.');
      return;
    }

    const payload: RegistrationData = {
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      email: email.trim().toLowerCase(),
      password,
      promotionSubscription,
    };

    if (street || city || state || zipCode) {
      payload.address = { street, city, state, zipCode };
    }

    if (cardNumber.trim()) {
      payload.paymentCard = {
        cardholderName: cardholderName || `${firstName} ${lastName}`,
        cardNumber: cardNumber.trim(),
        cardType,
        expirationDate: expirationDate.trim(),
        billingAddress: payload.address,
      };
    }

    setLoading(true);
    try {
      await register(payload);
      setRegisteredEmail(payload.email);

      // Attempt to retrieve latest verification link from backend dev outbox
      try {
        const linkRes = await fetchLatestVerification(payload.email);
        if (linkRes.actionUrl) {
          setVerificationUrl(linkRes.actionUrl);
        }
      } catch {
        // ignore dev link fetch error
      }
    } catch (err: any) {
      setError(err?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fadeIn overflow-y-auto">
      <div className="relative w-full max-w-lg bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-2xl my-8">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-muted hover:text-white transition-colors"
          aria-label="Close modal"
        >
          <X className="w-5 h-5" />
        </button>

        {registeredEmail ? (
          <div className="text-center py-4">
            <div className="w-14 h-14 bg-green-500/10 text-green-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-green-500/20">
              <CheckCircle className="w-8 h-8" />
            </div>
            <h2 className="text-2xl font-bold text-white tracking-tight mb-2">Account Created!</h2>
            <p className="text-sm text-gray-300 mb-2">
              A verification email has been sent to <span className="font-semibold text-white">{registeredEmail}</span>.
            </p>
            <div className="p-3 bg-amber-500/10 border border-amber-500/20 rounded-xl text-amber-300 text-xs text-left mb-6">
              <p className="font-semibold mb-1">Account Status: Inactive (Unverified)</p>
              <p>Per system security policy, your account remains Inactive until you verify your email.</p>
            </div>

            {verificationUrl && (
              <div className="mb-6 p-4 bg-primary/10 border border-primary/20 rounded-xl text-left">
                <p className="text-xs font-semibold text-primary mb-1 flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4" /> Demo Quick Action:
                </p>
                <p className="text-xs text-muted mb-3">Simulated confirmation email link captured:</p>
                <a
                  href={verificationUrl}
                  onClick={() => onClose()}
                  className="inline-block py-2 px-3 bg-primary text-white text-xs font-medium rounded-lg hover:bg-blue-600 transition-colors"
                >
                  Verify Email Now (TC3)
                </a>
              </div>
            )}

            <div className="flex gap-3 justify-center">
              <button
                onClick={() => {
                  setRegisteredEmail(null);
                  onClose();
                  onOpenLogin();
                }}
                className="py-2.5 px-6 bg-surface hover:bg-white/5 border border-white/10 text-white rounded-xl text-sm font-medium transition-all"
              >
                Go to Sign In
              </button>
            </div>
          </div>
        ) : (
          <>
            <div className="mb-6 text-center">
              <h2 className="text-2xl font-bold text-white tracking-tight">Create Account</h2>
              <p className="text-sm text-muted mt-1">Join CinemaFlex for seamless movie booking</p>
            </div>

            {error && (
              <div className="mb-4 p-3.5 bg-red-500/10 border border-red-500/20 rounded-xl flex items-start gap-3 text-red-400 text-sm">
                <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
                <p>{error}</p>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4 max-h-[70vh] overflow-y-auto pr-1">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1">
                    First Name <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
                    <input
                      type="text"
                      required
                      value={firstName}
                      onChange={e => setFirstName(e.target.value)}
                      placeholder="First Name"
                      className="w-full pl-9 pr-3 py-2 bg-background border border-white/10 rounded-xl text-white placeholder-muted focus:outline-none focus:border-primary text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1">
                    Last Name <span className="text-red-400">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    value={lastName}
                    onChange={e => setLastName(e.target.value)}
                    placeholder="Last Name"
                    className="w-full px-3 py-2 bg-background border border-white/10 rounded-xl text-white placeholder-muted focus:outline-none focus:border-primary text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">
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
                    className="w-full pl-9 pr-3 py-2 bg-background border border-white/10 rounded-xl text-white placeholder-muted focus:outline-none focus:border-primary text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">
                  Password <span className="text-red-400">*</span>
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    placeholder="At least 8 chars with uppercase, digits, symbols"
                    className="w-full pl-9 pr-9 py-2 bg-background border border-white/10 rounded-xl text-white placeholder-muted focus:outline-none focus:border-primary text-sm"
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

                {password && (
                  <div className="mt-2 space-y-1">
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-muted">Strength:</span>
                      <span className="font-semibold text-white">{getStrengthLabel()}</span>
                    </div>
                    <div className="h-1.5 w-full bg-white/5 rounded-full overflow-hidden">
                      <div
                        className={`h-full transition-all duration-300 ${getStrengthColor()}`}
                        style={{ width: `${(strengthScore / 5) * 100}%` }}
                      />
                    </div>
                    <div className="grid grid-cols-2 gap-1 text-[11px] text-muted pt-1">
                      <span className={hasMinLength ? 'text-green-400' : ''}>✓ 8+ characters</span>
                      <span className={hasUpper ? 'text-green-400' : ''}>✓ Uppercase letter</span>
                      <span className={hasLower ? 'text-green-400' : ''}>✓ Lowercase letter</span>
                      <span className={hasDigit ? 'text-green-400' : ''}>✓ Number</span>
                      <span className={hasSpecial ? 'text-green-400' : ''}>✓ Special symbol (!@#$%^&*)</span>
                    </div>
                  </div>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">
                  Confirm Password <span className="text-red-400">*</span>
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    value={confirmPassword}
                    onChange={e => setConfirmPassword(e.target.value)}
                    placeholder="Repeat password"
                    className="w-full pl-9 pr-3 py-2 bg-background border border-white/10 rounded-xl text-white placeholder-muted focus:outline-none focus:border-primary text-sm"
                  />
                </div>
              </div>

              <div className="p-3 bg-white/5 border border-white/10 rounded-xl">
                <label className="flex items-center gap-2.5 cursor-pointer text-xs text-gray-200">
                  <input
                    type="checkbox"
                    checked={promotionSubscription}
                    onChange={e => setPromotionSubscription(e.target.checked)}
                    className="w-4 h-4 rounded text-primary focus:ring-primary bg-background border-white/20"
                  />
                  <span>Opt-in to receive promotional emails and discounts (Recommended)</span>
                </label>
              </div>

              {/* Collapsible Optional Section */}
              <div className="border border-white/10 rounded-xl overflow-hidden">
                <button
                  type="button"
                  onClick={() => setShowOptionalFields(!showOptionalFields)}
                  className="w-full py-2.5 px-3.5 bg-white/5 hover:bg-white/10 flex items-center justify-between text-xs font-medium text-gray-300 transition-colors"
                >
                  <span className="flex items-center gap-2">
                    <MapPin className="w-3.5 h-3.5 text-primary" />
                    Optional: Add Address & Payment Card Now
                  </span>
                  {showOptionalFields ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                </button>

                {showOptionalFields && (
                  <div className="p-3.5 space-y-3 bg-background/50 border-t border-white/10 text-xs">
                    <p className="font-semibold text-gray-300">Home Address (Optional)</p>
                    <input
                      type="text"
                      placeholder="Street Address"
                      value={street}
                      onChange={e => setStreet(e.target.value)}
                      className="w-full px-3 py-2 bg-background border border-white/10 rounded-lg text-white"
                    />
                    <div className="grid grid-cols-3 gap-2">
                      <input
                        type="text"
                        placeholder="City"
                        value={city}
                        onChange={e => setCity(e.target.value)}
                        className="w-full px-3 py-2 bg-background border border-white/10 rounded-lg text-white"
                      />
                      <input
                        type="text"
                        placeholder="State (e.g. GA)"
                        value={state}
                        onChange={e => setState(e.target.value)}
                        className="w-full px-3 py-2 bg-background border border-white/10 rounded-lg text-white"
                      />
                      <input
                        type="text"
                        placeholder="Zip Code"
                        value={zipCode}
                        onChange={e => setZipCode(e.target.value)}
                        className="w-full px-3 py-2 bg-background border border-white/10 rounded-lg text-white"
                      />
                    </div>

                    <p className="font-semibold text-gray-300 pt-2 flex items-center gap-1.5">
                      <CreditCard className="w-3.5 h-3.5 text-primary" /> Payment Card (Optional - Encrypted with AES)
                    </p>
                    <input
                      type="text"
                      placeholder="Cardholder Name"
                      value={cardholderName}
                      onChange={e => setCardholderName(e.target.value)}
                      className="w-full px-3 py-2 bg-background border border-white/10 rounded-lg text-white"
                    />
                    <input
                      type="text"
                      placeholder="Card Number"
                      value={cardNumber}
                      onChange={e => setCardNumber(e.target.value)}
                      className="w-full px-3 py-2 bg-background border border-white/10 rounded-lg text-white"
                    />
                    <div className="grid grid-cols-2 gap-2">
                      <select
                        value={cardType}
                        onChange={e => setCardType(e.target.value)}
                        className="w-full px-3 py-2 bg-background border border-white/10 rounded-lg text-white"
                      >
                        <option value="Visa">Visa</option>
                        <option value="MasterCard">MasterCard</option>
                        <option value="Amex">American Express</option>
                        <option value="Discover">Discover</option>
                      </select>
                      <input
                        type="text"
                        placeholder="Exp (MM/YY)"
                        value={expirationDate}
                        onChange={e => setExpirationDate(e.target.value)}
                        className="w-full px-3 py-2 bg-background border border-white/10 rounded-lg text-white"
                      />
                    </div>
                  </div>
                )}
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 px-4 bg-primary hover:bg-blue-600 disabled:opacity-50 text-white font-medium rounded-xl text-sm transition-all shadow-lg shadow-primary/20 flex items-center justify-center gap-2 mt-4"
              >
                {loading ? (
                  <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  'Create Account'
                )}
              </button>
            </form>

            <div className="mt-4 text-center text-xs text-muted">
              Already have an account?{' '}
              <button
                onClick={() => {
                  onClose();
                  onOpenLogin();
                }}
                className="text-primary hover:underline font-semibold ml-1"
              >
                Sign In
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};
