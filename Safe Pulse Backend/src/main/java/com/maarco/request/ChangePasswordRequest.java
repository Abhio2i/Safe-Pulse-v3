/**
 * Data transfer object (DTO) for password change requests.
 * Contains the required fields for changing a user's password:
 * - User's email (for identification)
 * - Current password (for verification)
 * - New password (to be set)
 */

package com.maarco.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    private String email;
    private String oldPassword;
    private String newPassword;
}
