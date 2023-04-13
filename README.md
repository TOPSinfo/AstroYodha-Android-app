# Kotlin - AstroYodha
This repository is a demonstration of a booking system for astrologers that includes features such as the ability to book an astrologer based on their speciality, as well as the ability to chat and video call with them.

# Description

In the Welcome screen, You have two options for user application. You can continue as astrologer or you can continue as user.User/Astrologer can login or register using social media and mobile number.

##### User Type : Astrologer

In the Dashboard screen astrologers can see a list of appointments.

In the Booking list Screen, the user can see all their booking done by the user.

In the Booking Detail screen, users can see all the details of booking and also initiate a chat and video call(one-to-one) with an astrologer.

In the Chat screen, an astrologer can chat with the user by sending Text, images, and video. Also, the user can initiate a video call(one-to-one) from here.

In the Profile screen, the user can manage his/her profile according to his/her speciality and availability.

##### User Type : User

In the Dashboard screen users can see a list of astrologers and also book appointments from the dashboard.

In the astrologer list screen, users can see a list of all the registered astrologers and book them.

In Add Booking screen , user can add new booking by adding information like tile og booking, booking date, booking time, event duration and personal detail like user photo, birth date, birth time,place of birth and photo of kundali.After adding all detail user can book astrologer.

In the Booking list Screen, the user can see all their booking.

In the Booking Detail screen, users can see all the details of booking and also initiate a chat and video call(one-to-one) with an astrologer.

Also we provide a wallet screen to manage wallet balance.

In the Profile screen, the user can manage his/her profile.

In the Chat screen, users can chat with astrologers by sending Text, images, and video. Also users can initiate a video call(one-to-one) from here.


# Table of Contents

- Welcome UI: It will provide option to select user type (Astrologer and User)
- Login UI: It will validate phone number and verify otp and redirect to Dashboard to appropriate user type
- Registration UI: It will collect user data and redirect to Dashboard to appropriate user type
- Dashboard UI: It will display a list of booking for astrologer and list of astrologer for user
- Create Booking UI: It will collect user data and create a new booking.
- Booking List UI: It will list the booking.
- Booking Detail UI: It will display details of a particular booking.
- Chat UI: It will display conversation(one-to-one), also button to initiate video call(one-to-one)
- Profile UI: It will display profile details of registered users.

# UI controls

- Firebase Authentication
- Firebase Firestore Database
- Jitsi Meet
- Recyclerview
- Activity
- ImageView
- Calender
- EditText
- TextView
- Buttons

# Technical detail:

- Project Architecture - MVVM
- Project language - Kotlin
- Database - Firebase Firestore
- Video Call tool - Jitsi Meet
- Minimum SDK Version - 23 (Android 6-Marshmallow)


# Documentation:

Jitsi:- https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-android-sdk/