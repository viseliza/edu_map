-- EduMap recommended schema hardening.
-- Run manually in Supabase SQL Editor after reviewing the comments.

create index if not exists courses_specialty_id_idx
  on public.courses(specialty_id);

create index if not exists semesters_course_id_idx
  on public.semesters(course_id);

create index if not exists disciplines_semester_id_idx
  on public.disciplines(semester_id);

create index if not exists topics_discipline_id_idx
  on public.topics(discipline_id);

create index if not exists topics_discipline_order_idx
  on public.topics(discipline_id, order_index);

create index if not exists materials_topic_id_idx
  on public.materials(topic_id);

create index if not exists materials_discipline_id_idx
  on public.materials(discipline_id);

-- Optional but recommended for cleaner content ordering.
alter table public.disciplines
  add column if not exists order_index integer;

alter table public.materials
  add column if not exists order_index integer;

alter table public.topics
  add column if not exists description text;

alter table public.specialties
  add column if not exists created_at timestamptz not null default now(),
  add column if not exists updated_at timestamptz not null default now();

alter table public.courses
  add column if not exists created_at timestamptz not null default now(),
  add column if not exists updated_at timestamptz not null default now();

alter table public.semesters
  add column if not exists created_at timestamptz not null default now(),
  add column if not exists updated_at timestamptz not null default now();

alter table public.disciplines
  add column if not exists created_at timestamptz not null default now(),
  add column if not exists updated_at timestamptz not null default now();

alter table public.topics
  add column if not exists created_at timestamptz not null default now(),
  add column if not exists updated_at timestamptz not null default now();

alter table public.materials
  add column if not exists created_at timestamptz not null default now(),
  add column if not exists updated_at timestamptz not null default now();

-- Keep material rows attached to at least one visible place in the app.
do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'materials_has_owner_check'
  ) then
    alter table public.materials
      add constraint materials_has_owner_check
      check (topic_id is not null or discipline_id is not null);
  end if;
end $$;

-- Suggested RLS direction:
-- 1. Enable public read for student-facing published content.
-- 2. Allow insert/update/delete only to authenticated admin users.
-- 3. Do not expose service_role key in Android or AdminPanel.
--
-- Exact admin policy depends on your auth setup. A common approach is an
-- admin_profiles table that marks allowed user ids, then policies check
-- exists(select 1 from public.admin_profiles where user_id = auth.uid()).
