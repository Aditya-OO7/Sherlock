# Sherlock - The Password Manager

## About

Sherlock is the password manager android application. Sherlock uses **PBKDF2** key derivation function with **SHA512** encryption technique for authentication and secure data storage.

## Screenshots

<table>
  <tr>
    <td><img src="docs/screenshots/RegisterScreen.png" alt="Register Screen image"></td>
    <td><img src="docs/screenshots/LoginScreen.png" alt="Login Screen image"></td>
    <td><img src="docs/screenshots/AddEditScreen.png" alt="Add Edit Screen image"></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/HomeScreen.png" alt="Home Screen image"></td>
    <td><img src="docs/screenshots/DetailScreen.png" alt="Detail Screen image"></td>
    <td><img src="docs/screenshots/ResetPasswordScreen.png" alt="Reset Password Screen image"></td>
  </tr>
</table>


## Built with

- Kotlin : First class and official programming language for Android development.
- Android Architecture Components :
  - LiveData : Data types that implement observer pattern.
  - ViewModel : Handles UI logic and helps to maintain state during configuration changes.
  - Room : SQLite persistance library.
- Coroutines : For asynchronous programming
- Navigation components : For better navigation handling.
- Espresso : UI testing framework.
- JUnit4 : Unit testing framework

## Developed with

- [MVVM](https://developer.android.com/jetpack/docs/guide#recommended-app-arch) architecture
- Clean architecture
- Test driven development
- Monolithic architecture

  ![](https://developer.android.com/topic/libraries/architecture/images/final-architecture.png)


## Contribute

If you want to contribute to this app, you're always welcome!
See [Contributing Guidelines](CONTRIBUTING.md).