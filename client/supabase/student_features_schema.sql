-- EduMap student features schema.
-- Run this file manually in Supabase SQL Editor after the existing base/admin scripts.
--
-- What it adds:
-- - rich discipline content for the new technical specification;
-- - student profiles with unique logins and real recovery emails;
-- - private saved disciplines and saved topics/notes for authenticated users.
-- - legacy password recovery requests by student login.

create extension if not exists citext;

alter table public.disciplines
  add column if not exists terminology text,
  add column if not exists theory text,
  add column if not exists application_area text;

create table if not exists public.profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  login citext not null unique,
  email citext,
  display_name text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint profiles_login_length_check check (char_length(login::text) between 3 and 32),
  constraint profiles_email_format_check check (email is null or email::text ~* '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$')
);

alter table public.profiles
  add column if not exists email citext;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'profiles_email_format_check'
      and conrelid = 'public.profiles'::regclass
  ) then
    alter table public.profiles
      add constraint profiles_email_format_check
      check (email is null or email::text ~* '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$');
  end if;
end $$;

create table if not exists public.favorite_disciplines (
  user_id uuid not null references auth.users(id) on delete cascade,
  discipline_id uuid not null references public.disciplines(id) on delete cascade,
  note text,
  created_at timestamptz not null default now(),
  primary key (user_id, discipline_id)
);

create table if not exists public.favorite_topics (
  user_id uuid not null references auth.users(id) on delete cascade,
  topic_id uuid not null references public.topics(id) on delete cascade,
  note text,
  created_at timestamptz not null default now(),
  primary key (user_id, topic_id)
);

create table if not exists public.password_reset_requests (
  id bigint generated always as identity primary key,
  login citext not null,
  status text not null default 'new',
  admin_note text,
  created_at timestamptz not null default now(),
  handled_at timestamptz,
  constraint password_reset_requests_login_length_check check (char_length(login::text) between 3 and 32),
  constraint password_reset_requests_login_format_check check (login::text ~ '^[a-z0-9._-]+$'),
  constraint password_reset_requests_status_check check (status in ('new', 'in_progress', 'done', 'rejected'))
);

create index if not exists profiles_login_idx
  on public.profiles(login);

create unique index if not exists profiles_email_idx
  on public.profiles(email)
  where email is not null;

create index if not exists favorite_disciplines_user_id_idx
  on public.favorite_disciplines(user_id);

create index if not exists favorite_disciplines_discipline_id_idx
  on public.favorite_disciplines(discipline_id);

create index if not exists favorite_topics_user_id_idx
  on public.favorite_topics(user_id);

create index if not exists favorite_topics_topic_id_idx
  on public.favorite_topics(topic_id);

create index if not exists password_reset_requests_status_created_at_idx
  on public.password_reset_requests(status, created_at desc);

create unique index if not exists password_reset_requests_open_login_idx
  on public.password_reset_requests(login)
  where status = 'new';

alter table public.profiles enable row level security;
alter table public.favorite_disciplines enable row level security;
alter table public.favorite_topics enable row level security;
alter table public.password_reset_requests enable row level security;

drop policy if exists "Users can read own profile" on public.profiles;
create policy "Users can read own profile"
  on public.profiles
  for select
  to authenticated
  using (user_id = auth.uid());

drop policy if exists "Users can create own profile" on public.profiles;
create policy "Users can create own profile"
  on public.profiles
  for insert
  to authenticated
  with check (user_id = auth.uid());

drop policy if exists "Users can update own profile" on public.profiles;
create policy "Users can update own profile"
  on public.profiles
  for update
  to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

drop policy if exists "Users can delete own profile" on public.profiles;
create policy "Users can delete own profile"
  on public.profiles
  for delete
  to authenticated
  using (user_id = auth.uid());

drop policy if exists "Users can read own favorite disciplines" on public.favorite_disciplines;
create policy "Users can read own favorite disciplines"
  on public.favorite_disciplines
  for select
  to authenticated
  using (user_id = auth.uid());

drop policy if exists "Users can insert own favorite disciplines" on public.favorite_disciplines;
create policy "Users can insert own favorite disciplines"
  on public.favorite_disciplines
  for insert
  to authenticated
  with check (user_id = auth.uid());

drop policy if exists "Users can update own favorite disciplines" on public.favorite_disciplines;
create policy "Users can update own favorite disciplines"
  on public.favorite_disciplines
  for update
  to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

drop policy if exists "Users can delete own favorite disciplines" on public.favorite_disciplines;
create policy "Users can delete own favorite disciplines"
  on public.favorite_disciplines
  for delete
  to authenticated
  using (user_id = auth.uid());

drop policy if exists "Users can read own favorite topics" on public.favorite_topics;
create policy "Users can read own favorite topics"
  on public.favorite_topics
  for select
  to authenticated
  using (user_id = auth.uid());

drop policy if exists "Users can insert own favorite topics" on public.favorite_topics;
create policy "Users can insert own favorite topics"
  on public.favorite_topics
  for insert
  to authenticated
  with check (user_id = auth.uid());

drop policy if exists "Users can update own favorite topics" on public.favorite_topics;
create policy "Users can update own favorite topics"
  on public.favorite_topics
  for update
  to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

drop policy if exists "Users can delete own favorite topics" on public.favorite_topics;
create policy "Users can delete own favorite topics"
  on public.favorite_topics
  for delete
  to authenticated
  using (user_id = auth.uid());

drop policy if exists "Anyone can create password reset request" on public.password_reset_requests;
create policy "Anyone can create password reset request"
  on public.password_reset_requests
  for insert
  to anon, authenticated
  with check (
    status = 'new'
    and admin_note is null
    and handled_at is null
  );

drop policy if exists "Admins can read password reset requests" on public.password_reset_requests;
create policy "Admins can read password reset requests"
  on public.password_reset_requests
  for select
  to authenticated
  using (
    exists (
      select 1
      from public.admin_profiles
      where user_id = auth.uid()
    )
  );

drop policy if exists "Admins can update password reset requests" on public.password_reset_requests;
create policy "Admins can update password reset requests"
  on public.password_reset_requests
  for update
  to authenticated
  using (
    exists (
      select 1
      from public.admin_profiles
      where user_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1
      from public.admin_profiles
      where user_id = auth.uid()
    )
  );

create or replace function public.is_login_available(candidate_login text)
returns boolean
language sql
security definer
set search_path = public
as $$
  select not exists (
    select 1
    from public.profiles
    where login = candidate_login::citext
  );
$$;

revoke all on function public.is_login_available(text) from public;
grant execute on function public.is_login_available(text) to anon, authenticated;
