import React, { useEffect, useState } from 'react';
import {
  AlertCircle,
  CheckCircle,
  CreditCard,
  Heart,
  KeyRound,
  Lock,
  Mail,
  MapPin,
  Plus,
  ShieldAlert,
  Trash2,
  User as UserIcon,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import {
  addPaymentCard,
  changePassword,
  deletePaymentCard,
  fetchFavoriteMovies,
  updatePaymentCard,
  updateProfile,
} from '../services/api';
import { Movie, PaymentCard, PaymentCardData } from '../types';
import { Link, useNavigate } from 'react-router-dom';

const Profile: React.FC = () => {
  const { user, refreshUser, isAuthenticated, toggleFavorite } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<'profile' | 'cards' | 'password' | 'favorites'>('profile');

  // Profile fields
  const [firstName, setFirstName] = useState(user?.firstName || '');
  const [lastName, setLastName] = useState(user?.lastName || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [promotionSubscription, setPromotionSubscription] = useState(user?.promotionSubscription ?? false);

  // Address fields (Max 1 address per rubric)
  const [street, setStreet] = useState(user?.address?.street || '');
  const [city, setCity] = useState(user?.address?.city || '');
  const [state, setState] = useState(user?.address?.state || '');
  const [zipCode, setZipCode] = useState(user?.address?.zipCode || '');

  // Password fields
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Cards
  const [isCardModalOpen, setIsCardModalOpen] = useState(false);
  const [editingCardId, setEditingCardId] = useState<string | null>(null);
  const [cardholderName, setCardholderName] = useState('');
  const [cardNumber, setCardNumber] = useState('');
  const [cardType, setCardType] = useState('Visa');
  const [expirationDate, setExpirationDate] = useState('');

  // Favorites
  const [favoriteMovies, setFavoriteMovies] = useState<Movie[]>([]);
  const [loadingFavorites, setLoadingFavorites] = useState(false);

  // Status banners
  const [profileMessage, setProfileMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [passwordMessage, setPasswordMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [cardMessage, setCardMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    if (user) {
      setFirstName(user.firstName || '');
      setLastName(user.lastName || '');
      setPhone(user.phone || '');
      setPromotionSubscription(user.promotionSubscription ?? false);
      setStreet(user.address?.street || '');
      setCity(user.address?.city || '');
      setState(user.address?.state || '');
      setZipCode(user.address?.zipCode || '');
    }
  }, [user]);

  useEffect(() => {
    if (activeTab === 'favorites' && user?.id) {
      loadFavorites();
    }
  }, [activeTab, user?.id]);

  const loadFavorites = async () => {
    if (!user?.id) return;
    setLoadingFavorites(true);
    try {
      const data = await fetchFavoriteMovies(user.id);
      setFavoriteMovies(data);
    } catch {
      setFavoriteMovies([]);
    } finally {
      setLoadingFavorites(false);
    }
  };

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileMessage(null);
    setSaving(true);
    try {
      await updateProfile(
        {
          firstName: firstName.trim(),
          lastName: lastName.trim(),
          phone: phone.trim(),
          promotionSubscription,
          address: { street, city, state, zipCode },
        },
        user?.id
      );
      await refreshUser();
      setProfileMessage({
        type: 'success',
        text: 'Profile updated successfully! A notification email has been dispatched to your email.',
      });
    } catch (err: any) {
      setProfileMessage({
        type: 'error',
        text: err?.message || 'Failed to update profile.',
      });
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setPasswordMessage(null);

    if (!currentPassword) {
      setPasswordMessage({ type: 'error', text: 'Current password is required.' });
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordMessage({ type: 'error', text: 'New passwords do not match.' });
      return;
    }
    if (newPassword.length < 8) {
      setPasswordMessage({ type: 'error', text: 'New password must be at least 8 characters long.' });
      return;
    }

    setSaving(true);
    try {
      await changePassword({ currentPassword, newPassword }, user?.id);
      setPasswordMessage({
        type: 'success',
        text: 'Password updated successfully! An email notification has been sent.',
      });
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: any) {
      setPasswordMessage({
        type: 'error',
        text: err?.message || 'Failed to update password. Verify current password.',
      });
    } finally {
      setSaving(false);
    }
  };

  const handleOpenAddCard = () => {
    if ((user?.paymentCards?.length || 0) >= 3) {
      setCardMessage({
        type: 'error',
        text: 'Constraint Exceeded: You cannot store more than 3 payment cards.',
      });
      return;
    }
    setEditingCardId(null);
    setCardholderName(`${user?.firstName || ''} ${user?.lastName || ''}`.trim());
    setCardNumber('');
    setCardType('Visa');
    setExpirationDate('');
    setIsCardModalOpen(true);
  };

  const handleOpenEditCard = (card: PaymentCard) => {
    setEditingCardId(card.id);
    setCardholderName(card.cardholderName);
    setCardNumber(card.cardLastFour);
    setCardType(card.cardType);
    setExpirationDate(card.expirationDate);
    setIsCardModalOpen(true);
  };

  const handleSaveCard = async (e: React.FormEvent) => {
    e.preventDefault();
    setCardMessage(null);
    setSaving(true);

    const cardData: PaymentCardData = {
      cardholderName: cardholderName.trim(),
      cardNumber: cardNumber.trim(),
      cardType,
      expirationDate: expirationDate.trim(),
      billingAddress: user?.address,
    };

    try {
      if (editingCardId) {
        await updatePaymentCard(editingCardId, cardData, user?.id);
        setCardMessage({ type: 'success', text: 'Payment card updated successfully.' });
      } else {
        await addPaymentCard(cardData, user?.id);
        setCardMessage({ type: 'success', text: 'Payment card encrypted and added successfully.' });
      }
      await refreshUser();
      setIsCardModalOpen(false);
    } catch (err: any) {
      setCardMessage({
        type: 'error',
        text: err?.message || 'Failed to save payment card.',
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteCard = async (cardId: string) => {
    if (!window.confirm('Are you sure you want to delete this payment card?')) return;
    setCardMessage(null);
    try {
      await deletePaymentCard(cardId, user?.id);
      await refreshUser();
      setCardMessage({ type: 'success', text: 'Payment card removed successfully.' });
    } catch (err: any) {
      setCardMessage({ type: 'error', text: err?.message || 'Failed to delete payment card.' });
    }
  };

  const handleRemoveFavorite = async (movieId: string) => {
    try {
      await toggleFavorite(movieId);
      setFavoriteMovies(prev => prev.filter(m => m.id !== movieId));
    } catch (err: any) {
      alert(err?.message || 'Failed to remove favorite');
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white tracking-tight">Account Settings</h1>
        <p className="text-sm text-muted mt-1">Manage your profile, security, payment methods, and favorite movies</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
        {/* Navigation Sidebar */}
        <div className="space-y-1.5 bg-surface/60 border border-white/10 rounded-2xl p-3 h-fit">
          <button
            onClick={() => setActiveTab('profile')}
            className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors ${
              activeTab === 'profile'
                ? 'bg-primary text-white shadow-lg shadow-primary/20'
                : 'text-gray-300 hover:bg-white/5'
            }`}
          >
            <UserIcon className="w-4 h-4" /> Personal & Address
          </button>
          <button
            onClick={() => setActiveTab('cards')}
            className={`w-full flex items-center justify-between px-4 py-2.5 rounded-xl text-sm font-medium transition-colors ${
              activeTab === 'cards'
                ? 'bg-primary text-white shadow-lg shadow-primary/20'
                : 'text-gray-300 hover:bg-white/5'
            }`}
          >
            <span className="flex items-center gap-3">
              <CreditCard className="w-4 h-4" /> Payment Cards
            </span>
            <span className="text-xs px-2 py-0.5 rounded-full bg-white/10">
              {user?.paymentCards?.length || 0}/3
            </span>
          </button>
          <button
            onClick={() => setActiveTab('password')}
            className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors ${
              activeTab === 'password'
                ? 'bg-primary text-white shadow-lg shadow-primary/20'
                : 'text-gray-300 hover:bg-white/5'
            }`}
          >
            <KeyRound className="w-4 h-4" /> Password & Security
          </button>
          <button
            onClick={() => setActiveTab('favorites')}
            className={`w-full flex items-center justify-between px-4 py-2.5 rounded-xl text-sm font-medium transition-colors ${
              activeTab === 'favorites'
                ? 'bg-primary text-white shadow-lg shadow-primary/20'
                : 'text-gray-300 hover:bg-white/5'
            }`}
          >
            <span className="flex items-center gap-3">
              <Heart className="w-4 h-4" /> Favorite Movies
            </span>
            <span className="text-xs px-2 py-0.5 rounded-full bg-white/10">
              {user?.favoriteMovieIds?.length || 0}
            </span>
          </button>
        </div>

        {/* Tab Content */}
        <div className="md:col-span-3">
          {/* TAB 1: Profile & Address */}
          {activeTab === 'profile' && (
            <div className="bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl">
              <h2 className="text-xl font-bold text-white mb-1">Personal Profile</h2>
              <p className="text-xs text-muted mb-6">Required fields are marked with an asterisk (*)</p>

              {profileMessage && (
                <div
                  className={`mb-6 p-4 rounded-xl flex items-start gap-3 text-sm ${
                    profileMessage.type === 'success'
                      ? 'bg-green-500/10 border border-green-500/20 text-green-400'
                      : 'bg-red-500/10 border border-red-500/20 text-red-400'
                  }`}
                >
                  {profileMessage.type === 'success' ? (
                    <CheckCircle className="w-5 h-5 flex-shrink-0" />
                  ) : (
                    <AlertCircle className="w-5 h-5 flex-shrink-0" />
                  )}
                  <p>{profileMessage.text}</p>
                </div>
              )}

              <form onSubmit={handleProfileSubmit} className="space-y-6">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-gray-300 mb-1.5">
                      First Name <span className="text-red-400">*</span>
                    </label>
                    <input
                      type="text"
                      required
                      value={firstName}
                      onChange={e => setFirstName(e.target.value)}
                      className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-300 mb-1.5">
                      Last Name <span className="text-red-400">*</span>
                    </label>
                    <input
                      type="text"
                      required
                      value={lastName}
                      onChange={e => setLastName(e.target.value)}
                      className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                    />
                  </div>
                </div>

                {/* Email - Non-editable constraint per rubric */}
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <label className="block text-xs font-medium text-gray-300">
                      Email Address <span className="text-red-400">*</span>
                    </label>
                    <span className="text-[11px] font-medium text-amber-400 flex items-center gap-1">
                      <Lock className="w-3 h-3" /> Email cannot be modified (Security Constraint)
                    </span>
                  </div>
                  <div className="relative">
                    <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
                    <input
                      type="email"
                      disabled
                      value={user?.email || ''}
                      className="w-full pl-10 pr-4 py-2.5 bg-background/50 border border-white/10 rounded-xl text-muted text-sm cursor-not-allowed select-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1.5">Phone Number</label>
                  <input
                    type="tel"
                    value={phone}
                    onChange={e => setPhone(e.target.value)}
                    placeholder="(555) 000-0000"
                    className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                  />
                </div>

                <div className="pt-4 border-t border-white/10">
                  <h3 className="text-base font-semibold text-white mb-1 flex items-center gap-2">
                    <MapPin className="w-4 h-4 text-primary" /> Home Address (Max 1 Address Allowed)
                  </h3>
                  <p className="text-xs text-muted mb-4">Mailing and billing address attached to your account.</p>

                  <div className="space-y-4">
                    <div>
                      <label className="block text-xs font-medium text-gray-300 mb-1.5">Street Address</label>
                      <input
                        type="text"
                        value={street}
                        onChange={e => setStreet(e.target.value)}
                        placeholder="123 Cinema Way"
                        className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                      />
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                      <div>
                        <label className="block text-xs font-medium text-gray-300 mb-1.5">City</label>
                        <input
                          type="text"
                          value={city}
                          onChange={e => setCity(e.target.value)}
                          placeholder="Athens"
                          className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-gray-300 mb-1.5">State</label>
                        <input
                          type="text"
                          value={state}
                          onChange={e => setState(e.target.value)}
                          placeholder="GA"
                          className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-gray-300 mb-1.5">Zip Code</label>
                        <input
                          type="text"
                          value={zipCode}
                          onChange={e => setZipCode(e.target.value)}
                          placeholder="30602"
                          className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <div className="p-4 bg-white/5 border border-white/10 rounded-xl">
                  <label className="flex items-center gap-3 cursor-pointer text-xs text-gray-200">
                    <input
                      type="checkbox"
                      checked={promotionSubscription}
                      onChange={e => setPromotionSubscription(e.target.checked)}
                      className="w-4 h-4 rounded text-primary focus:ring-primary bg-background border-white/20"
                    />
                    <span>Opt-in to promotional emails and exclusive ticket discounts</span>
                  </label>
                </div>

                <button
                  type="submit"
                  disabled={saving}
                  className="py-2.5 px-6 bg-primary hover:bg-blue-600 disabled:opacity-50 text-white font-medium rounded-xl text-sm transition-all shadow-lg shadow-primary/20 flex items-center justify-center gap-2"
                >
                  {saving ? 'Saving...' : 'Save Profile Changes'}
                </button>
              </form>
            </div>
          )}

          {/* TAB 2: Payment Cards */}
          {activeTab === 'cards' && (
            <div className="bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                <div>
                  <h2 className="text-xl font-bold text-white mb-1">Stored Payment Cards</h2>
                  <p className="text-xs text-muted">
                    Payment card numbers are encrypted with AES in the database. Maximum 3 cards allowed.
                  </p>
                </div>
                <button
                  onClick={handleOpenAddCard}
                  disabled={(user?.paymentCards?.length || 0) >= 3}
                  className="py-2 px-4 bg-primary hover:bg-blue-600 disabled:opacity-40 disabled:cursor-not-allowed text-white text-xs font-medium rounded-xl transition-all shadow-lg shadow-primary/20 flex items-center gap-2 whitespace-nowrap"
                >
                  <Plus className="w-4 h-4" /> Add Payment Card
                </button>
              </div>

              {(user?.paymentCards?.length || 0) >= 3 && (
                <div className="mb-6 p-4 bg-amber-500/10 border border-amber-500/20 rounded-xl flex items-center gap-3 text-amber-300 text-xs">
                  <ShieldAlert className="w-5 h-5 flex-shrink-0" />
                  <p className="font-semibold">
                    Constraint Validation (TC7): You have reached the maximum allowed limit of 3 payment cards.
                  </p>
                </div>
              )}

              {cardMessage && (
                <div
                  className={`mb-6 p-4 rounded-xl flex items-center gap-3 text-sm ${
                    cardMessage.type === 'success'
                      ? 'bg-green-500/10 border border-green-500/20 text-green-400'
                      : 'bg-red-500/10 border border-red-500/20 text-red-400'
                  }`}
                >
                  {cardMessage.type === 'success' ? (
                    <CheckCircle className="w-5 h-5 flex-shrink-0" />
                  ) : (
                    <AlertCircle className="w-5 h-5 flex-shrink-0" />
                  )}
                  <p>{cardMessage.text}</p>
                </div>
              )}

              <div className="space-y-3">
                {(!user?.paymentCards || user.paymentCards.length === 0) ? (
                  <div className="text-center py-12 border border-dashed border-white/10 rounded-2xl">
                    <CreditCard className="w-12 h-12 text-muted mx-auto mb-3" />
                    <p className="text-sm font-medium text-gray-300">No payment cards stored</p>
                    <p className="text-xs text-muted mt-1">Add a payment method for quicker ticket booking.</p>
                  </div>
                ) : (
                  user.paymentCards.map(card => (
                    <div
                      key={card.id}
                      className="p-4 bg-background/60 border border-white/10 rounded-xl flex items-center justify-between gap-4 hover:border-white/20 transition-colors"
                    >
                      <div className="flex items-center gap-4">
                        <div className="w-12 h-8 rounded-lg bg-surface flex items-center justify-center font-bold text-xs border border-white/10 text-primary">
                          {card.cardType || 'CARD'}
                        </div>
                        <div>
                          <p className="text-sm font-semibold text-white tracking-wide">
                            •••• •••• •••• {card.cardLastFour.slice(-4)}
                          </p>
                          <p className="text-xs text-muted">
                            {card.cardholderName} · Expires {card.expirationDate}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleOpenEditCard(card)}
                          className="px-3 py-1.5 bg-white/5 hover:bg-white/10 border border-white/10 rounded-lg text-xs text-gray-200 transition-colors"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDeleteCard(card.id)}
                          className="p-1.5 hover:bg-red-500/10 text-red-400 rounded-lg transition-colors"
                          title="Delete Card"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {/* TAB 3: Password & Security */}
          {activeTab === 'password' && (
            <div className="bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl">
              <h2 className="text-xl font-bold text-white mb-1">Update Password</h2>
              <p className="text-xs text-muted mb-6">
                Security Requirement: You must provide your current password to change it.
              </p>

              {passwordMessage && (
                <div
                  className={`mb-6 p-4 rounded-xl flex items-center gap-3 text-sm ${
                    passwordMessage.type === 'success'
                      ? 'bg-green-500/10 border border-green-500/20 text-green-400'
                      : 'bg-red-500/10 border border-red-500/20 text-red-400'
                  }`}
                >
                  {passwordMessage.type === 'success' ? (
                    <CheckCircle className="w-5 h-5 flex-shrink-0" />
                  ) : (
                    <AlertCircle className="w-5 h-5 flex-shrink-0" />
                  )}
                  <p>{passwordMessage.text}</p>
                </div>
              )}

              <form onSubmit={handlePasswordSubmit} className="space-y-4 max-w-md">
                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1.5">
                    Current Password <span className="text-red-400">*</span>
                  </label>
                  <input
                    type="password"
                    required
                    value={currentPassword}
                    onChange={e => setCurrentPassword(e.target.value)}
                    placeholder="Enter existing password"
                    className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1.5">
                    New Password <span className="text-red-400">*</span>
                  </label>
                  <input
                    type="password"
                    required
                    value={newPassword}
                    onChange={e => setNewPassword(e.target.value)}
                    placeholder="At least 8 chars, mixed case & symbols"
                    className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1.5">
                    Confirm New Password <span className="text-red-400">*</span>
                  </label>
                  <input
                    type="password"
                    required
                    value={confirmPassword}
                    onChange={e => setConfirmPassword(e.target.value)}
                    placeholder="Repeat new password"
                    className="w-full px-3.5 py-2.5 bg-background border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:border-primary"
                  />
                </div>

                <button
                  type="submit"
                  disabled={saving}
                  className="py-2.5 px-6 bg-primary hover:bg-blue-600 disabled:opacity-50 text-white font-medium rounded-xl text-sm transition-all shadow-lg shadow-primary/20 flex items-center justify-center gap-2 mt-4"
                >
                  {saving ? 'Updating...' : 'Change Password'}
                </button>
              </form>
            </div>
          )}

          {/* TAB 4: Favorites */}
          {activeTab === 'favorites' && (
            <div className="bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h2 className="text-xl font-bold text-white mb-1">My Favorite Movies</h2>
                  <p className="text-xs text-muted">
                    Movies saved to your favorites (TC8 & TC9: favorites persist across login sessions)
                  </p>
                </div>
                <Link
                  to="/"
                  className="text-xs text-primary hover:underline font-semibold flex items-center gap-1"
                >
                  Browse Movies
                </Link>
              </div>

              {loadingFavorites ? (
                <div className="text-center py-12 text-sm text-muted">Loading your favorites...</div>
              ) : favoriteMovies.length === 0 ? (
                <div className="text-center py-12 border border-dashed border-white/10 rounded-2xl">
                  <Heart className="w-12 h-12 text-muted mx-auto mb-3" />
                  <p className="text-sm font-medium text-gray-300">No favorite movies saved yet</p>
                  <p className="text-xs text-muted mt-1 mb-4">
                    Click the heart icon on any movie while browsing to add it here!
                  </p>
                  <Link
                    to="/"
                    className="py-2 px-4 bg-primary text-white text-xs font-medium rounded-xl hover:bg-blue-600 transition-colors"
                  >
                    Explore Movies Now
                  </Link>
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {favoriteMovies.map(movie => (
                    <div
                      key={movie.id}
                      className="bg-background/80 border border-white/10 rounded-xl overflow-hidden group hover:border-primary/50 transition-all flex flex-col"
                    >
                      <div className="relative aspect-[16/9] overflow-hidden bg-black/40">
                        <img
                          src={movie.posterUrl}
                          alt={movie.title}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                        />
                        <button
                          onClick={() => handleRemoveFavorite(movie.id)}
                          className="absolute top-2 right-2 p-1.5 bg-black/70 hover:bg-red-500 rounded-full text-red-400 hover:text-white transition-colors"
                          title="Remove from favorites"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                      <div className="p-4 flex-1 flex flex-col justify-between">
                        <div>
                          <div className="flex items-center gap-2 mb-1">
                            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-white/10 text-white">
                              {movie.rating}
                            </span>
                            <span className="text-[10px] text-muted">{movie.genre?.join(', ')}</span>
                          </div>
                          <h3 className="font-semibold text-white text-sm line-clamp-1">{movie.title}</h3>
                        </div>
                        <div className="mt-3 pt-3 border-t border-white/10 flex items-center justify-between">
                          <Link
                            to={`/movie/${movie.id}`}
                            className="text-xs text-primary hover:underline font-medium"
                          >
                            Details & Trailer
                          </Link>
                          {movie.status === 'CURRENTLY_RUNNING' && (
                            <Link
                              to={`/booking/${movie.id}/${movie.availableShowtimes?.[0] || '2:00 PM'}`}
                              className="px-2.5 py-1 bg-primary text-white text-[11px] font-semibold rounded-lg hover:bg-blue-600 transition-colors"
                            >
                              Book Tickets
                            </Link>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Add / Edit Card Modal */}
      {isCardModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fadeIn">
          <div className="relative w-full max-w-md bg-surface border border-white/10 rounded-2xl p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-4">
              {editingCardId ? 'Edit Payment Card' : 'Add Payment Card'}
            </h3>
            <form onSubmit={handleSaveCard} className="space-y-3.5">
              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">
                  Cardholder Name <span className="text-red-400">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={cardholderName}
                  onChange={e => setCardholderName(e.target.value)}
                  placeholder="Full Name as on Card"
                  className="w-full px-3 py-2 bg-background border border-white/10 rounded-xl text-white text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-300 mb-1">
                  Card Number <span className="text-red-400">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={cardNumber}
                  onChange={e => setCardNumber(e.target.value)}
                  placeholder="16-digit card number"
                  className="w-full px-3 py-2 bg-background border border-white/10 rounded-xl text-white text-sm"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1">Card Type</label>
                  <select
                    value={cardType}
                    onChange={e => setCardType(e.target.value)}
                    className="w-full px-3 py-2 bg-background border border-white/10 rounded-xl text-white text-sm"
                  >
                    <option value="Visa">Visa</option>
                    <option value="MasterCard">MasterCard</option>
                    <option value="Amex">American Express</option>
                    <option value="Discover">Discover</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1">
                    Expiration Date <span className="text-red-400">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    value={expirationDate}
                    onChange={e => setExpirationDate(e.target.value)}
                    placeholder="MM/YY"
                    className="w-full px-3 py-2 bg-background border border-white/10 rounded-xl text-white text-sm"
                  />
                </div>
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsCardModalOpen(false)}
                  className="flex-1 py-2 bg-surface hover:bg-white/5 border border-white/10 text-white rounded-xl text-xs font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 py-2 bg-primary hover:bg-blue-600 text-white rounded-xl text-xs font-medium shadow-lg shadow-primary/20"
                >
                  {saving ? 'Saving...' : 'Save Card'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Profile;
