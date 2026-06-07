-- EduMap sample data for end-to-end testing.
-- Run manually in Supabase SQL Editor after reviewing.
-- This uses fixed UUIDs so it can be re-run safely with upserts.

insert into public.specialties (id, name)
values
  ('11111111-1111-1111-1111-111111111111', 'Информационные системы и программирование')
on conflict (id) do update set
  name = excluded.name;

insert into public.courses (id, number, specialty_id)
values
  ('22222222-2222-2222-2222-222222222221', 1, '11111111-1111-1111-1111-111111111111'),
  ('22222222-2222-2222-2222-222222222222', 2, '11111111-1111-1111-1111-111111111111')
on conflict (id) do update set
  number = excluded.number,
  specialty_id = excluded.specialty_id;

insert into public.semesters (id, number, course_id)
values
  ('33333333-3333-3333-3333-333333333331', 1, '22222222-2222-2222-2222-222222222221'),
  ('33333333-3333-3333-3333-333333333332', 2, '22222222-2222-2222-2222-222222222221'),
  ('33333333-3333-3333-3333-333333333333', 3, '22222222-2222-2222-2222-222222222222')
on conflict (id) do update set
  number = excluded.number,
  course_id = excluded.course_id;

insert into public.disciplines (id, name, description, semester_id, order_index)
values
  (
    '44444444-4444-4444-4444-444444444441',
    'Основы программирования',
    'Базовые понятия алгоритмизации, синтаксиса языка и разработки простых программ.',
    '33333333-3333-3333-3333-333333333331',
    1
  ),
  (
    '44444444-4444-4444-4444-444444444442',
    'Android Development',
    'Разработка мобильных приложений для Android на Kotlin.',
    '33333333-3333-3333-3333-333333333333',
    1
  )
on conflict (id) do update set
  name = excluded.name,
  description = excluded.description,
  semester_id = excluded.semester_id,
  order_index = excluded.order_index;

insert into public.topics (id, name, order_index, discipline_id, description)
values
  (
    '55555555-5555-5555-5555-555555555551',
    'Переменные и типы данных',
    1,
    '44444444-4444-4444-4444-444444444441',
    'Типы данных, переменные, операции присваивания и простые выражения.'
  ),
  (
    '55555555-5555-5555-5555-555555555552',
    'Основы Jetpack Compose',
    1,
    '44444444-4444-4444-4444-444444444442',
    'Composable-функции, Material 3 и построение экранов Android-приложения.'
  )
on conflict (id) do update set
  name = excluded.name,
  order_index = excluded.order_index,
  discipline_id = excluded.discipline_id,
  description = excluded.description;

insert into public.materials (id, title, description, type, url, topic_id, discipline_id, order_index)
values
  (
    '66666666-6666-6666-6666-666666666661',
    'Документация Kotlin',
    'Официальный справочник по языку Kotlin.',
    'link',
    'https://kotlinlang.org/docs/home.html',
    '55555555-5555-5555-5555-555555555551',
    null,
    1
  ),
  (
    '66666666-6666-6666-6666-666666666662',
    'Jetpack Compose basics',
    'Официальный учебный материал по Jetpack Compose.',
    'article',
    'https://developer.android.com/develop/ui/compose/tutorial',
    '55555555-5555-5555-5555-555555555552',
    null,
    1
  ),
  (
    '66666666-6666-6666-6666-666666666663',
    'Android Developers',
    'Главный портал документации Android.',
    'link',
    'https://developer.android.com/',
    null,
    '44444444-4444-4444-4444-444444444442',
    2
  )
on conflict (id) do update set
  title = excluded.title,
  description = excluded.description,
  type = excluded.type,
  url = excluded.url,
  topic_id = excluded.topic_id,
  discipline_id = excluded.discipline_id,
  order_index = excluded.order_index;
