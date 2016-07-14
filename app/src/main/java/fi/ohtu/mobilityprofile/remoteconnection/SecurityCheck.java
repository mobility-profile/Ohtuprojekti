package fi.ohtu.mobilityprofile.remoteconnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

import com.commonsware.cwac.security.PermissionLint;
import com.commonsware.cwac.security.PermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * If two Android applications define the same permission, the application that is installed first
 * is the one whose definition is used. Because of this we need to check if other applications
 * have defined the permission we use to prevent any security problems. It is sufficient to do
 * that check only the first time the application is run.
 *
 * If there are conflicts, we shouldn't give any information to any applications before those
 * conflicts are solved. Only way to solve them is to uninstall the applications causing the
 * conflicts and run Mobility Profile after doing so. Then those applications can be safely
 * installed again (although any application shouldn't cause conflicts at all, and application that
 * is doing so is basically trying to hack Mobility Profile).
 */
public class SecurityCheck {
    /**
     * Tells if there are any security problems.
     *
     * @param sharedPreferences Shared preferences
     * @return True if there are no security problems, false otherwise
     */
    public static boolean securityCheckOk(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("securitycheck", false);
    }

    /**
     * Performs the security check and returns true if no problems were found.
     *
     * @param context Application context
     * @param sharedPreferences Shared preferences
     * @return True if no problems were found, false otherwise
     */
    public static boolean doSecurityCheck(Context context, SharedPreferences sharedPreferences) {
        if (hasNoConflicts(context)) {
            sharedPreferences.edit().putBoolean("securitycheck", true).apply();
            return true;
        }

        return false;
    }

    /**
     * Returns a list of applications that are causing security problems.
     *
     * @param context Application context
     * @return List of conflicting applications
     */
    public static List<PackageInfo> getConflictInfo(Context context) {
        List<PackageInfo> conflictApps = new ArrayList<>();

        Map<PackageInfo, ArrayList<PermissionLint>> customPermissions = PermissionUtils.checkCustomPermissions(context);

        for (PackageInfo packageInfo : customPermissions.keySet()) {
            for (PermissionLint permissionLint : customPermissions.get(packageInfo)) {
                if (hasPermissionConflict(permissionLint)) {
                    conflictApps.add(packageInfo);
                }
            }
        }

        return conflictApps;
    }

    /**
     * Checks if there are any security problems.
     *
     * @param context Application context
     * @return true if there were no problems, false otherwise
     */
    private static boolean hasNoConflicts(Context context) {
        return getConflictInfo(context).isEmpty();
    }

    /**
     * Checks if a permission is in conflict with our definition.
     *
     * @param permissionLint Permission to check
     * @return True if there is a conflict, false otherwise
     */
    private static boolean hasPermissionConflict(PermissionLint permissionLint) {
        return permissionLint.wasDowngraded || permissionLint.signaturesDiffer
                || permissionLint.wasUpgraded || permissionLint.proseDiffers;
    }
}
