# Supabase setup for EduMap

В этой папке лежат SQL-скрипты для укрепления схемы Supabase под мобильное приложение и AdminPanel.

## Порядок

1. Проверьте схему и данные в Supabase Dashboard.
2. Выполните `recommended_schema_hardening.sql` в SQL Editor.
3. Создайте администратора в Supabase Auth.
4. Выполните `admin_rls_policies.sql` в SQL Editor.
5. Выполните `student_features_schema.sql` в SQL Editor, чтобы добавить профили студентов, избранное/заметки и расширенные поля дисциплин.
6. Выполните `storage_materials_policies.sql` в SQL Editor, если преподаватели будут загружать PDF, презентации или документы.
7. Добавьте `user_id` администратора в `public.admin_profiles`.
8. Для проверки всех экранов можно выполнить `sample_seed_data.sql`.

Скрипт не содержит service role key и не требует секретов.
