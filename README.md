# 4. Spring

---

### Цель работы
Добавить в user-service поддержку Spring и разработать API, которое позволит управлять данными.

Требования:
- Использовать необходимые модули spring(boot, web, data etc).
- Реализовать api для получения, создания, обновления и удаления юзера. Важно, entity не должен возвращаться из контроллера, необходимо использовать dto.
- Заменить Hibernate на Spring data JPA.
- Написать тесты для API(можно делать это при помощи mockMvc или других средств)

### Результат
https://github.com/user-attachments/assets/04fd4622-9d49-4f54-aa66-65e512391448

#### Тестовые запросы

1. Создание пользователей
Запрос 1:
<img width="1276" height="797" alt="Screenshot_2" src="https://github.com/user-attachments/assets/78fbadcc-5512-4d18-8cfd-51589a911d6f" />
Запрос 2:
<img width="1277" height="795" alt="Screenshot_3" src="https://github.com/user-attachments/assets/0e5f670c-2dca-479a-9ea6-7b5a9ac218b1" />
Запрос 3:
<img width="1278" height="796" alt="Screenshot_5" src="https://github.com/user-attachments/assets/dc61aa13-b1c5-4b31-ac07-f88f50a0d3b5" />

2. Получение всех пользователей
<img width="1277" height="798" alt="Screenshot_6" src="https://github.com/user-attachments/assets/0cb4f108-8232-4188-ab86-e7b4dc0e67c5" />

3. Получение пользователя по ID
<img width="1278" height="797" alt="Screenshot_7" src="https://github.com/user-attachments/assets/6b26b8c5-a241-41ec-bf99-f20926b0e7b1" />

4. Обновление пользователя
<img width="1279" height="798" alt="Screenshot_8" src="https://github.com/user-attachments/assets/a84968e4-e06c-44d0-8934-7f6f46d38061" />

5. Удаление пользователя
<img width="1280" height="797" alt="Screenshot_10" src="https://github.com/user-attachments/assets/90efccb8-0f7d-48ca-9f5e-6786f0ca17ec" />

6. Дубликат email
<img width="1280" height="797" alt="Screenshot_12" src="https://github.com/user-attachments/assets/727c7205-18b5-409f-ab53-3713ddf28f78" />

7. Невалидные данные
<img width="1275" height="795" alt="Screenshot_11" src="https://github.com/user-attachments/assets/d22480f3-f5e4-43c3-a47e-a7333dd829f8" />

8. Пользователь не найден
<img width="1279" height="796" alt="Screenshot_12" src="https://github.com/user-attachments/assets/b540d147-bdb1-43c6-8d42-6690c81e7d4a" />

#### Тестовое покрытие
<img width="1763" height="275" alt="Screenshot_13" src="https://github.com/user-attachments/assets/f5679675-38f5-4ea2-b50e-312f8275fc9c" />
