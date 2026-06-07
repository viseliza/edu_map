-- EduMap Supabase Storage setup for learning materials.
-- Run manually in Supabase SQL Editor after admin_rls_policies.sql.

insert into storage.buckets (
  id,
  name,
  public,
  file_size_limit,
  allowed_mime_types
)
values (
  'materials',
  'materials',
  true,
  52428800,
  array[
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'image/png',
    'image/jpeg'
  ]
)
on conflict (id) do update set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "Public can read material files" on storage.objects;
create policy "Public can read material files"
  on storage.objects
  for select
  to anon, authenticated
  using (bucket_id = 'materials');

drop policy if exists "Admins can upload material files" on storage.objects;
create policy "Admins can upload material files"
  on storage.objects
  for insert
  to authenticated
  with check (
    bucket_id = 'materials'
    and exists (
      select 1
      from public.admin_profiles
      where user_id = auth.uid()
    )
  );

drop policy if exists "Admins can update material files" on storage.objects;
create policy "Admins can update material files"
  on storage.objects
  for update
  to authenticated
  using (
    bucket_id = 'materials'
    and exists (
      select 1
      from public.admin_profiles
      where user_id = auth.uid()
    )
  )
  with check (
    bucket_id = 'materials'
    and exists (
      select 1
      from public.admin_profiles
      where user_id = auth.uid()
    )
  );

drop policy if exists "Admins can delete material files" on storage.objects;
create policy "Admins can delete material files"
  on storage.objects
  for delete
  to authenticated
  using (
    bucket_id = 'materials'
    and exists (
      select 1
      from public.admin_profiles
      where user_id = auth.uid()
    )
  );
