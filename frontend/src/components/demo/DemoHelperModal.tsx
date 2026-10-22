import React, { useEffect, useState } from 'react';
import {
  CheckCircle,
  Copy,
  ExternalLink,
  Inbox,
  RefreshCw,
  Sparkles,
  X,
} from 'lucide-react';
import { fetchDevEmails, resetDemoUsers } from '../../services/api';
import { SentEmail } from '../../types';

interface DemoHelperModalProps {
  isOpen: boolean;
  onClose: () => void;
  onFillRegister: (data: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    promotionSubscription: boolean;
  }) => void;
  onFillLogin: (email: string, pass: string) => void;
}

export const DemoHelperModal: React.FC<DemoHelperModalProps> = ({
  isOpen,
  onClose,
  onFillRegister,
  onFillLogin,
}) => {
  const [activeTab, setActiveTab] = useState<'cases' | 'outbox' | 'seeds'>('cases');
  const [emails, setEmails] = useState<SentEmail[]>([]);
  const [loadingEmails, setLoadingEmails] = useState(false);
  const [copiedText, setCopiedText] = useState<string | null>(null);
  const [resetting, setResetting] = useState(false);
  const [resetSuccess, setResetSuccess] = useState(false);

  useEffect(() => {
    if (isOpen) {
      loadEmails();
    }
  }, [isOpen]);

  const loadEmails = async () => {
    setLoadingEmails(true);
    try {
      const list = await fetchDevEmails();
      setEmails(list);
    } catch {
      // ignore
    } finally {
      setLoadingEmails(false);
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedText(text);
    setTimeout(() => setCopiedText(null), 2000);
  };

  const handleResetDemoData = async () => {
    setResetting(true);
    setResetSuccess(false);
    try {
      await resetDemoUsers();
      setResetSuccess(true);
      setTimeout(() => setResetSuccess(false), 3000);
    } catch {
      // ignore
    } finally {
      setResetting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-3xl bg-surface border border-primary/30 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
        {/* Modal Header */}
        <div className="p-5 border-b border-white/10 flex items-center justify-between bg-primary/10">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-primary/20 text-primary rounded-xl">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white tracking-tight flex items-center gap-2">
                Sprint 2 Demo Companion & Test Runner
              </h2>
              <p className="text-xs text-muted">Test cases TC1–TC10 & pre-seeded accounts</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-muted hover:text-white rounded-lg hover:bg-white/5 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Bar */}
        <div className="flex border-b border-white/10 px-5 pt-3 gap-2 bg-surface">
          <button
            onClick={() => setActiveTab('cases')}
            className={`px-4 py-2 text-xs font-semibold rounded-t-xl transition-all border-b-2 ${
              activeTab === 'cases'
                ? 'border-primary text-primary bg-primary/5'
                : 'border-transparent text-muted hover:text-white'
            }`}
          >
            Core Test Cases (TC1 – TC10)
          </button>
          <button
            onClick={() => {
              setActiveTab('outbox');
              loadEmails();
            }}
            className={`px-4 py-2 text-xs font-semibold rounded-t-xl transition-all border-b-2 flex items-center gap-1.5 ${
              activeTab === 'outbox'
                ? 'border-primary text-primary bg-primary/5'
                : 'border-transparent text-muted hover:text-white'
            }`}
          >
            <Inbox className="w-3.5 h-3.5" /> Simulated Email Outbox ({emails.length})
          </button>
          <button
            onClick={() => setActiveTab('seeds')}
            className={`px-4 py-2 text-xs font-semibold rounded-t-xl transition-all border-b-2 ${
              activeTab === 'seeds'
                ? 'border-primary text-primary bg-primary/5'
                : 'border-transparent text-muted hover:text-white'
            }`}
          >
            Data Seeds Reference
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 overflow-y-auto space-y-4 flex-1 text-xs">
          {/* TAB 1: TEST CASES */}
          {activeTab === 'cases' && (
            <div className="space-y-3.5">
              {/* TC1 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <span className="font-bold text-primary">TC1: Create User Account (Unverified)</span>
                  <p className="text-gray-300">
                    Register Eman Saleh (eman@gmail.my). No address, no cards, Promo: Yes.
                  </p>
                  <p className="text-muted text-[11px]">
                    Expected: Account created successfully, status = INACTIVE, confirmation email sent.
                  </p>
                </div>
                <button
                  onClick={() => {
                    onClose();
                    onFillRegister({
                      firstName: 'Eman',
                      lastName: 'Saleh',
                      email: 'eman@gmail.my',
                      password: 'MyPass!20',
                      promotionSubscription: true,
                    });
                  }}
                  className="px-3 py-2 bg-primary text-white rounded-lg font-medium hover:bg-blue-600 transition-colors whitespace-nowrap shadow"
                >
                  Fill Registration (TC1)
                </button>
              </div>

              {/* TC2 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <span className="font-bold text-amber-400">TC2: Login with Unverified Account</span>
                  <p className="text-gray-300">
                    Attempt login with Eman Saleh (<code className="text-white">eman@gmail.my</code>).
                  </p>
                  <p className="text-muted text-[11px]">
                    Expected message: "Account is not verified. Please check your email to verify your account." Login is denied.
                  </p>
                </div>
                <button
                  onClick={() => {
                    onClose();
                    onFillLogin('eman@gmail.my', 'MyPass!20');
                  }}
                  className="px-3 py-2 bg-amber-500/20 text-amber-300 border border-amber-500/30 rounded-lg font-medium hover:bg-amber-500/30 transition-colors whitespace-nowrap"
                >
                  Test Unverified Login (TC2)
                </button>
              </div>

              {/* TC3 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <span className="font-bold text-green-400">TC3: Verify Account via Email Link</span>
                  <p className="text-gray-300">
                    Use verification link sent to eman@gmail.my to activate account.
                  </p>
                  <p className="text-muted text-[11px]">
                    Expected: Account status becomes ACTIVE. User can proceed to login.
                  </p>
                </div>
                <button
                  onClick={() => {
                    setActiveTab('outbox');
                    loadEmails();
                  }}
                  className="px-3 py-2 bg-green-500/20 text-green-300 border border-green-500/30 rounded-lg font-medium hover:bg-green-500/30 transition-colors whitespace-nowrap"
                >
                  View Outbox Link (TC3)
                </button>
              </div>

              {/* TC4 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <span className="font-bold text-blue-400">TC4: Login with Verified User</span>
                  <p className="text-gray-300">
                    Login with Sarah Jenkins (<code className="text-white">favuser@gmail.com</code> / <code className="text-white">UserPass!20</code>).
                  </p>
                  <p className="text-muted text-[11px]">
                    Expected: Successful login, full navigation to user home page.
                  </p>
                </div>
                <button
                  onClick={() => {
                    onClose();
                    onFillLogin('favuser@gmail.com', 'UserPass!20');
                  }}
                  className="px-3 py-2 bg-primary/20 text-primary border border-primary/30 rounded-lg font-medium hover:bg-primary/30 transition-colors whitespace-nowrap"
                >
                  Login Verified User (TC4)
                </button>
              </div>

              {/* TC5 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <span className="font-bold text-red-400">TC5: Invalid Login Attempt</span>
                  <p className="text-gray-300">
                    Login with incorrect password (<code className="text-white">favuser@gmail.com</code> / <code className="text-white">WrongPass!99</code>).
                  </p>
                  <p className="text-muted text-[11px]">
                    Expected: Error message displayed, login denied.
                  </p>
                </div>
                <button
                  onClick={() => {
                    onClose();
                    onFillLogin('favuser@gmail.com', 'WrongPass!99');
                  }}
                  className="px-3 py-2 bg-red-500/20 text-red-300 border border-red-500/30 rounded-lg font-medium hover:bg-red-500/30 transition-colors whitespace-nowrap"
                >
                  Test Invalid Login (TC5)
                </button>
              </div>

              {/* TC6 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <span className="font-bold text-purple-400">TC6: Login with Admin Account</span>
                  <p className="text-gray-300">
                    Login as System Admin (<code className="text-white">admin@cinema.com</code> / <code className="text-white">AdminPass!20</code>).
                  </p>
                  <p className="text-muted text-[11px]">
                    Expected: Successful login, redirected to Admin portal with Manage Movies, Promotions, Users, Showtimes.
                  </p>
                </div>
                <button
                  onClick={() => {
                    onClose();
                    onFillLogin('admin@cinema.com', 'AdminPass!20');
                  }}
                  className="px-3 py-2 bg-purple-500/20 text-purple-300 border border-purple-500/30 rounded-lg font-medium hover:bg-purple-500/30 transition-colors whitespace-nowrap"
                >
                  Login as Admin (TC6)
                </button>
              </div>

              {/* TC7 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <span className="font-bold text-amber-400">TC7: Edit Profile (3 Cards Constraint)</span>
                  <p className="text-gray-300">
                    Login with Alex Rivera (<code className="text-white">cardsuser@gmail.com</code> / <code className="text-white">UserPass!20</code>).
                  </p>
                  <p className="text-muted text-[11px]">
                    Expected: 3 cards displayed; system blocks adding a 4th card; email is non-editable; update/delete card works.
                  </p>
                </div>
                <button
                  onClick={() => {
                    onClose();
                    onFillLogin('cardsuser@gmail.com', 'UserPass!20');
                  }}
                  className="px-3 py-2 bg-amber-500/20 text-amber-300 border border-amber-500/30 rounded-lg font-medium hover:bg-amber-500/30 transition-colors whitespace-nowrap"
                >
                  Login 3-Cards User (TC7)
                </button>
              </div>

              {/* TC8 & TC9 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl space-y-2">
                <span className="font-bold text-pink-400">TC8 & TC9: Favorites (Heart Icon & Persistence)</span>
                <p className="text-gray-300">
                  Browse movies on the homepage. Click the heart icon (with tooltip) on any movie to favorite it.
                  Log out and log back in to verify the favorites persist in MongoDB.
                </p>
              </div>

              {/* TC10 */}
              <div className="p-4 bg-background/60 border border-white/10 rounded-xl space-y-2">
                <span className="font-bold text-emerald-400">TC10: Security Validation</span>
                <p className="text-gray-300">
                  • Passwords hashed with BCrypt (strength 10 rounds).<br />
                  • Payment cards encrypted with AES in MongoDB (<code className="text-white">cardNumberEncrypted</code>).<br />
                  • Password changes require current password.<br />
                  • Profile change email notification sent automatically.
                </p>
              </div>
            </div>
          )}

          {/* TAB 2: OUTBOX */}
          {activeTab === 'outbox' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <p className="text-xs text-muted">
                  Recorded outgoing emails from backend (Verification, Password Reset, Profile Changes):
                </p>
                <button
                  onClick={loadEmails}
                  className="flex items-center gap-1.5 px-2.5 py-1 bg-white/5 hover:bg-white/10 border border-white/10 rounded-lg text-xs text-gray-200"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${loadingEmails ? 'animate-spin' : ''}`} /> Refresh
                </button>
              </div>

              {emails.length === 0 ? (
                <div className="text-center py-10 border border-dashed border-white/10 rounded-xl text-muted">
                  No emails sent yet in this session. Register an account or request a password reset to see it here!
                </div>
              ) : (
                <div className="space-y-3">
                  {emails.map((mail, idx) => (
                    <div
                      key={idx}
                      className="p-4 bg-background/60 border border-white/10 rounded-xl space-y-2"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span className="font-bold text-white text-xs">{mail.subject}</span>
                        <span className="text-[10px] px-2 py-0.5 rounded-full bg-white/10 font-mono text-muted">
                          {mail.type}
                        </span>
                      </div>
                      <p className="text-gray-300 text-xs">
                        To: <span className="text-white font-mono">{mail.to}</span>
                      </p>
                      <div className="p-2.5 bg-background border border-white/5 rounded-lg text-[11px] font-mono text-muted whitespace-pre-wrap max-h-32 overflow-y-auto">
                        {mail.body}
                      </div>
                      {mail.actionUrl && (
                        <div className="pt-1 flex items-center justify-between">
                          <a
                            href={mail.actionUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center gap-1.5 text-xs text-primary font-semibold hover:underline"
                          >
                            Execute Email Action Link <ExternalLink className="w-3.5 h-3.5" />
                          </a>
                          <button
                            onClick={() => copyToClipboard(mail.actionUrl || '')}
                            className="text-[11px] text-muted hover:text-white flex items-center gap-1"
                          >
                            <Copy className="w-3 h-3" />
                            {copiedText === mail.actionUrl ? 'Copied!' : 'Copy Link'}
                          </button>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 3: SEED ACCOUNTS */}
          {activeTab === 'seeds' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <p className="text-xs text-muted">Pre-seeded accounts ready for demo testing:</p>
                <button
                  onClick={handleResetDemoData}
                  disabled={resetting}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-primary/20 text-primary border border-primary/30 rounded-lg text-xs font-semibold hover:bg-primary/30"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${resetting ? 'animate-spin' : ''}`} /> Reset Seed Data
                </button>
              </div>

              {resetSuccess && (
                <div className="p-3 bg-green-500/10 border border-green-500/20 text-green-400 rounded-xl text-xs flex items-center gap-2">
                  <CheckCircle className="w-4 h-4" /> Seed data successfully restored!
                </div>
              )}

              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-white/5 text-muted uppercase text-[10px] border-b border-white/10">
                    <tr>
                      <th className="py-2.5 px-3">Role / Purpose</th>
                      <th className="py-2.5 px-3">Email</th>
                      <th className="py-2.5 px-3">Password</th>
                      <th className="py-2.5 px-3">Pre-Loaded Data</th>
                      <th className="py-2.5 px-3 text-right">Quick Fill</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/5">
                    <tr>
                      <td className="py-3 px-3 font-semibold text-purple-400">Admin Account</td>
                      <td className="py-3 px-3 font-mono">admin@cinema.com</td>
                      <td className="py-3 px-3 font-mono">AdminPass!20</td>
                      <td className="py-3 px-3 text-muted">Admin Portal access</td>
                      <td className="py-3 px-3 text-right">
                        <button
                          onClick={() => {
                            onClose();
                            onFillLogin('admin@cinema.com', 'AdminPass!20');
                          }}
                          className="px-2.5 py-1 bg-white/5 hover:bg-white/10 rounded text-gray-200"
                        >
                          Use
                        </button>
                      </td>
                    </tr>
                    <tr>
                      <td className="py-3 px-3 font-semibold text-blue-400">Verified User (Favorites)</td>
                      <td className="py-3 px-3 font-mono">favuser@gmail.com</td>
                      <td className="py-3 px-3 font-mono">UserPass!20</td>
                      <td className="py-3 px-3 text-muted">2 Favorite movies saved</td>
                      <td className="py-3 px-3 text-right">
                        <button
                          onClick={() => {
                            onClose();
                            onFillLogin('favuser@gmail.com', 'UserPass!20');
                          }}
                          className="px-2.5 py-1 bg-white/5 hover:bg-white/10 rounded text-gray-200"
                        >
                          Use
                        </button>
                      </td>
                    </tr>
                    <tr>
                      <td className="py-3 px-3 font-semibold text-amber-400">Verified User (3 Cards)</td>
                      <td className="py-3 px-3 font-mono">cardsuser@gmail.com</td>
                      <td className="py-3 px-3 font-mono">UserPass!20</td>
                      <td className="py-3 px-3 text-muted">3 Encrypted payment cards (TC7)</td>
                      <td className="py-3 px-3 text-right">
                        <button
                          onClick={() => {
                            onClose();
                            onFillLogin('cardsuser@gmail.com', 'UserPass!20');
                          }}
                          className="px-2.5 py-1 bg-white/5 hover:bg-white/10 rounded text-gray-200"
                        >
                          Use
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
