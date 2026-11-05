import React, { useState } from 'react';
import {
  User,
  Mail,
  Lock,
  DollarSign,
  ShieldCheck,
  Calendar,
  AlertCircle,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { Card, CardHeader, CardContent } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Input } from '../../components/common/Input';
import { authService } from '../../services/authService';
import { formatDate } from '../../utils/dateFormatter';
import { checkPasswordStrength } from '../../utils/validators';

export const ProfilePage = () => {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });

  const [passwordErrors, setPasswordErrors] = useState({});
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [passwordServerErr, setPasswordServerErr] = useState('');

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData((prev) => ({ ...prev, [name]: value }));
    if (passwordErrors[name]) {
      setPasswordErrors((prev) => ({ ...prev, [name]: '' }));
    }
    if (passwordServerErr) setPasswordServerErr('');
  };

  const validatePasswordForm = () => {
    const errs = {};
    if (!passwordData.currentPassword) {
      errs.currentPassword = 'Current password is required';
    }
    if (!passwordData.newPassword) {
      errs.newPassword = 'New password is required';
    } else {
      const strength = checkPasswordStrength(passwordData.newPassword);
      if (!strength.isValid) {
        errs.newPassword =
          'New password must be 8+ chars with uppercase, lowercase, digit, and special char';
      }
    }
    if (passwordData.newPassword !== passwordData.confirmNewPassword) {
      errs.confirmNewPassword = 'Passwords do not match';
    }

    setPasswordErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    if (!validatePasswordForm()) return;

    setIsChangingPassword(true);
    setPasswordServerErr('');

    try {
      await authService.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });

      success('Password changed successfully!');
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: '',
      });
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        'Failed to change password. Please check your current password.';
      setPasswordServerErr(msg);
      toastError(msg);
    } finally {
      setIsChangingPassword(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
          User Profile & Settings
        </h2>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Review your account profile details and manage your security credentials.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Account Details */}
        <Card>
          <CardHeader
            title="Personal Information"
            subtitle="Registered account profile details"
          />
          <CardContent className="space-y-4">
            <div>
              <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                Full Name
              </label>
              <div className="mt-1 flex items-center gap-2.5 text-sm font-medium text-slate-800">
                <User className="w-4 h-4 text-slate-400" />
                <span>{user?.fullName || 'Not provided'}</span>
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                Email Address
              </label>
              <div className="mt-1 flex items-center gap-2.5 text-sm font-medium text-slate-800">
                <Mail className="w-4 h-4 text-slate-400" />
                <span>{user?.email || 'Not provided'}</span>
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                Default Currency
              </label>
              <div className="mt-1 flex items-center gap-2.5 text-sm font-medium text-slate-800">
                <DollarSign className="w-4 h-4 text-slate-400" />
                <span>{user?.preferredCurrency || 'USD'}</span>
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                Account Role
              </label>
              <div className="mt-1 flex items-center gap-2.5 text-sm font-medium text-slate-800">
                <ShieldCheck className="w-4 h-4 text-emerald-500" />
                <span>{user?.role || 'USER'}</span>
              </div>
            </div>

            {user?.createdAt && (
              <div>
                <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                  Member Since
                </label>
                <div className="mt-1 flex items-center gap-2.5 text-sm font-medium text-slate-800">
                  <Calendar className="w-4 h-4 text-slate-400" />
                  <span>{formatDate(user.createdAt)}</span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Change Password Card */}
        <Card>
          <CardHeader
            title="Change Password"
            subtitle="Ensure your account uses a secure password"
          />
          <CardContent>
            {passwordServerErr && (
              <div
                role="alert"
                className="mb-4 p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2"
              >
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{passwordServerErr}</span>
              </div>
            )}

            <form onSubmit={handlePasswordSubmit} className="space-y-4" noValidate>
              <Input
                label="Current Password"
                id="currentPassword"
                name="currentPassword"
                type="password"
                required
                value={passwordData.currentPassword}
                onChange={handlePasswordChange}
                error={passwordErrors.currentPassword}
                leftIcon={<Lock className="w-4 h-4" />}
              />

              <Input
                label="New Password"
                id="newPassword"
                name="newPassword"
                type="password"
                required
                value={passwordData.newPassword}
                onChange={handlePasswordChange}
                error={passwordErrors.newPassword}
                leftIcon={<Lock className="w-4 h-4" />}
              />

              <Input
                label="Confirm New Password"
                id="confirmNewPassword"
                name="confirmNewPassword"
                type="password"
                required
                value={passwordData.confirmNewPassword}
                onChange={handlePasswordChange}
                error={passwordErrors.confirmNewPassword}
                leftIcon={<Lock className="w-4 h-4" />}
              />

              <Button
                type="submit"
                variant="primary"
                size="md"
                className="w-full"
                isLoading={isChangingPassword}
              >
                Update Password
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

