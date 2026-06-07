# Auth settings for student login

EduMap keeps the student-facing flow simple:

- students register with `login`, real `email`, and `password`;
- students sign in with `login` and `password`;
- password recovery is requested by `login`, then Supabase sends a recovery email to the email saved in `public.profiles.email`.

The service role key is used only on Vercel API routes. It must never be placed in the Android app.

## Required Supabase SQL

Run `supabase/student_features_schema.sql` after base/admin scripts. The script adds:

- `public.profiles.email`;
- a unique index for non-empty profile emails;
- validation for profile email format;
- favorites tables for disciplines and topics.

## Required Supabase Dashboard setting

For quick student registration during development:

1. Open Supabase Dashboard.
2. Go to `Authentication` -> `Providers` -> `Email`.
3. Turn off `Confirm email`.
4. Save the settings.

If `Confirm email` is turned on later, registration will require email confirmation before a session is returned to the mobile app.

## Password recovery

The mobile app calls the Vercel endpoint:

```text
https://admin-panel-edumap.vercel.app/api/student-password-recovery
```

The endpoint finds the student's saved email by login and calls Supabase Auth recovery with this redirect:

```text
https://admin-panel-edumap.vercel.app/reset-password
```

Add this URL in Supabase Dashboard:

1. Open `Authentication` -> `URL Configuration`.
2. Add `https://admin-panel-edumap.vercel.app/reset-password` to `Redirect URLs`.
3. Save the settings.

The response is intentionally generic, so outsiders cannot check whether a login exists.

Legacy builds used a technical email like `<login>@example.com` and stored password reset requests for admins. That flow is no longer the main path and should not be shown to college staff in the admin panel.
