# Log

## 2024-04-07

1. For each added folder, call POST taggings
    ```json
    {
      "feed_id": "123",
      "name": "Test Folder"
    }
    ```

2. For each removed folder, call DELETE taggings
3. If feed name changed, call POST subscriptions
