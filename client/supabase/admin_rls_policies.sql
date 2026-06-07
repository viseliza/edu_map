-- EduMap RLS policies for student app + AdminPanel.
-- Review and run manually in Supabase SQL Editor.
--
-- Goal:
-- - anonymous/mobile users can read curriculum data;
-- - only authenticated users listed in public.admin_profiles can create, edit, delete.

create table if not exists public.admin_profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  created_at timestamptz not null default now()
);

alter table public.admin_profiles enable row level security;

drop policy if exists "Admins can read admin profiles" on public.admin_profiles;
create policy "Admins can read admin profiles"
  on public.admin_profiles
  for select
  to authenticated
  using (user_id = auth.uid());

-- Insert the admin user id manually after creating the user in Supabase Auth:
-- insert into public.admin_profiles (user_id) values ('00000000-0000-0000-0000-000000000000');

do $$
declare
  table_name text;
begin
  foreach table_name in array array[
    'specialties',
    'courses',
    'semesters',
    'disciplines',
    'topics',
    'materials'
  ]
  loop
    execute format('alter table public.%I enable row level security', table_name);

    execute format('drop policy if exists "Public can read %s" on public.%I', table_name, table_name);
    execute format(
      'create policy "Public can read %s" on public.%I for select to anon, authenticated using (true)',
      table_name,
      table_name
    );

    execute format('drop policy if exists "Admins can insert %s" on public.%I', table_name, table_name);
    execute format(
      'create policy "Admins can insert %s" on public.%I for insert to authenticated with check (exists (select 1 from public.admin_profiles where user_id = auth.uid()))',
      table_name,
      table_name
    );

    execute format('drop policy if exists "Admins can update %s" on public.%I', table_name, table_name);
    execute format(
      'create policy "Admins can update %s" on public.%I for update to authenticated using (exists (select 1 from public.admin_profiles where user_id = auth.uid())) with check (exists (select 1 from public.admin_profiles where user_id = auth.uid()))',
      table_name,
      table_name
    );

    execute format('drop policy if exists "Admins can delete %s" on public.%I', table_name, table_name);
    execute format(
      'create policy "Admins can delete %s" on public.%I for delete to authenticated using (exists (select 1 from public.admin_profiles where user_id = auth.uid()))',
      table_name,
      table_name
    );
  end loop;
end $$;
