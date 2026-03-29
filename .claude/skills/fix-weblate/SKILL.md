1. Open an existing checkout of the upstream repository or perform a fresh one.
2. Commit all pending changes in Weblate and lock the translation component.
    ```
    wlc commit capy-reader/strings; wlc lock capy-reader/strings
    ```
3. Add Weblate exported repository as a remote.
    ```
    git remote add weblate https://hosted.weblate.org/git/capy-reader/strings/ ; git remote update weblate
    ```
4. Merge Weblate changes and resolve any conflicts.
    ```
    git merge weblate/main
    ```
5. Push changes into upstream repository.
    ```
    git push origin main
    ```
6. Weblate should now be able to see updated repository and you can unlock it.
    ```
    wlc pull capy-reader/strings ; wlc unlock capy-reader/strings
    ```
