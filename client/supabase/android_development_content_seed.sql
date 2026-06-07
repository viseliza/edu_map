-- EduMap content seed for one complete discipline page.
-- Run this file in Supabase SQL Editor after the base schema and student_features_schema.sql.
-- It safely fills the existing "Android Development" discipline used by the demo data.

begin;

alter table public.disciplines
  add column if not exists terminology text,
  add column if not exists theory text,
  add column if not exists application_area text,
  add column if not exists order_index integer;

alter table public.topics
  add column if not exists order_index integer;

alter table public.materials
  add column if not exists order_index integer;

insert into public.disciplines (
  id,
  name,
  description,
  terminology,
  theory,
  application_area,
  semester_id,
  order_index
)
values (
  '44444444-4444-4444-4444-444444444442',
  'Android Development',
  'Разработка мобильных приложений для Android на Kotlin.',
  'Activity - экран или точка входа Android-приложения. Intent - объект для перехода между экранами и передачи данных. Composable - функция Jetpack Compose, которая описывает часть интерфейса. State - состояние экрана, от которого зависит отображение UI. ViewModel - слой, который хранит данные экрана и переживает поворот устройства.',
  'Android-приложение строится из экранов, состояния и источников данных. В современном подходе интерфейс часто описывают декларативно через Jetpack Compose: разработчик описывает, как экран должен выглядеть при текущем состоянии, а Compose сам обновляет нужные элементы. Kotlin используется как основной язык разработки, потому что он короче Java, безопаснее работает с null и хорошо поддерживается Google. Для устойчивого приложения важно разделять UI, бизнес-логику и работу с сетью или базой данных.',
  'Навыки Android-разработки применяются при создании мобильных приложений для колледжей, бизнеса, банков, доставки, медицины, расписаний, личных кабинетов и образовательных сервисов. В рамках EduMap эти знания помогают понять, как студентское приложение получает данные из Supabase, показывает дисциплины, материалы и избранное.',
  '33333333-3333-3333-3333-333333333333',
  1
)
on conflict (id) do update set
  name = excluded.name,
  description = excluded.description,
  terminology = excluded.terminology,
  theory = excluded.theory,
  application_area = excluded.application_area,
  semester_id = excluded.semester_id,
  order_index = excluded.order_index;

insert into public.topics (id, name, order_index, discipline_id)
values
  (
    '55555555-5555-5555-5555-555555555552',
    'Основы Android и Kotlin',
    1,
    '44444444-4444-4444-4444-444444444442'
  ),
  (
    '55555555-5555-5555-5555-555555555553',
    'Jetpack Compose',
    2,
    '44444444-4444-4444-4444-444444444442'
  ),
  (
    '55555555-5555-5555-5555-555555555554',
    'Навигация и экраны приложения',
    3,
    '44444444-4444-4444-4444-444444444442'
  ),
  (
    '55555555-5555-5555-5555-555555555555',
    'Работа с данными и Supabase',
    4,
    '44444444-4444-4444-4444-444444444442'
  )
on conflict (id) do update set
  name = excluded.name,
  order_index = excluded.order_index,
  discipline_id = excluded.discipline_id;

insert into public.materials (
  id,
  title,
  description,
  type,
  url,
  topic_id,
  discipline_id,
  order_index
)
values
  (
    '66666666-6666-6666-6666-666666666662',
    'Jetpack Compose basics',
    'Официальный учебный материал Google по созданию интерфейса на Jetpack Compose.',
    'article',
    'https://developer.android.com/develop/ui/compose/tutorial',
    '55555555-5555-5555-5555-555555555553',
    null,
    1
  ),
  (
    '66666666-6666-6666-6666-666666666663',
    'Android Developers',
    'Главный портал документации Android: архитектура, UI, навигация, хранение данных и публикация приложения.',
    'link',
    'https://developer.android.com/',
    null,
    '44444444-4444-4444-4444-444444444442',
    2
  ),
  (
    '66666666-6666-6666-6666-666666666664',
    'Kotlin для Android',
    'Раздел документации Kotlin о применении языка в Android-разработке.',
    'article',
    'https://kotlinlang.org/docs/android-overview.html',
    '55555555-5555-5555-5555-555555555552',
    null,
    3
  ),
  (
    '66666666-6666-6666-6666-666666666665',
    'Navigation in Compose',
    'Официальное руководство по переходам между экранами в приложениях на Compose.',
    'article',
    'https://developer.android.com/develop/ui/compose/navigation',
    '55555555-5555-5555-5555-555555555554',
    null,
    4
  ),
  (
    '66666666-6666-6666-6666-666666666666',
    'Supabase Kotlin',
    'Библиотека для подключения Kotlin и Android-приложений к Supabase.',
    'link',
    'https://supabase.com/docs/reference/kotlin/introduction',
    '55555555-5555-5555-5555-555555555555',
    null,
    5
  )
on conflict (id) do update set
  title = excluded.title,
  description = excluded.description,
  type = excluded.type,
  url = excluded.url,
  topic_id = excluded.topic_id,
  discipline_id = excluded.discipline_id,
  order_index = excluded.order_index;

commit;
