Original App Design Project - README Template
===

# Roomie

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Roomie is everything you need to have a good rooommate/housemate experience-- schedule chores, track expenses, create grocery lists and more!

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Organizer/Tools
- **Mobile:** The app is only available on mobile. Having the app on mobile makes it highly convenient and accessible. Mobile is essential to remind users to complete chores, to pay other users back, and more.
- **Story:** Keeps everyone accountable to do their part to create a positive roommate/housemate experience.
- **Market:** College students, anyone in communal housing
- **Habit:** Users are consistently using this app for their everyday tasks
- **Scope:**
  - V1: Create account, create circles, expense tracker, chore scheduler
  - V2: Notifications

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Create user account
  * Manage associated profile
  * Log in and log out
* Create circle
  * Join a circle using a code
* Schedule chores
  * Assign to users in circle
  * View my chores
  * View chore calendar
* Add house expenses
  * Who made purchase
  * How much other users owe purchaser
  * Sum up total amounts users owe eachother
* Settings
  * Edit profile
  * Log out
* API
  * Options:
    * Google Calendar with Google OAuth
    * SMS or email notifications

**Optional Nice-to-have Stories**

* Tokens associated to each chore
  * wheel of prizes
* Shopping list
  * Add items to shopping list
  * Categorize by low priority, medium priority, high priority
  * Add count
  * See which user(s) requested item
* Add profile image to user
* Control how often chores repeat
* Make chores rotating between users in circle
* Multiple chore calendar views
  * monthly
  * weekly
  * daily
* Push notifications
  * Automatic for uncompleted chores
  * Initiated by user (ask user to pay expense)
* Open venmo from house expenses screen
* In-app messaging within circle
* Streaks
* Admin priviledges
  * Approve new members joining
  * Remove members
  * Generate new circle code
* Join multiple circles
* OAuth 2.0 with Google

### 2. Screen Archetypes

* Login
  * Sign in
  * Button to create account
* Create account
  * Name
  * Phone number
  * Email
  * Password
* Dashboard before being added to circle
  * Create circle
  * Join circle --> open modal asking for circle code
* Create circle
  * Name circle
  * Generate join code
* Dashboard home
  * Circle name
  * Users in circle
  * Small notification view
  * Streak - based on daily chore completion
* My chore schedule
* Chore calendar
* Add chore
  * Form for customizations
* Chore detail
  * Detailed information about specific chore
* Expenses
  * List of ongoing house expenses - user still owes money
  * List of completed house expenses
  * Summary of calculations
* Add house expense
  * Form for customization
* Expense detail
* Settings
  * Account information
  * Logout button

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home
* Chores
* Expenses
* Settings

**Flow Navigation** (Screen to Screen)

* Home
  * Before having circle
    * Create circle
  * After having circle
    * Home screen for other circles joined
    * Expense detail
    * Chore detail
* Chores
  * Add chore
  * Chore detail
  * Chore calendar
* Expenses
  * Add expense
  * Expense detail
* Settings
  * Login
* Login
  * Create account
  * Home

## Temporary Schedule

### Week 1
Build out the skeleton of your project. Midpoint self review due Thursday.
- Set up backend database
- Create user account
- Login/logout
- Outline app (create template)
  - Set up toolbar
  - Bottom navigation
  - Outline screens
  - Implement navigation buttons
- Colors
- Logos
- Create Parse classes

### Week 2
Build out core features, with a focus on your difficult/ambiguous technical problems.
- Implement settings page
  - Edit account information
  - Edit circle information
- Create circle
- Join circle and view joined circles
- Implement home page
  - View circle details
  - View users in circle

### Week 3
Continue building core features, including ambiguous technical problems.
- House expense list and summary
- Add house expense
- View expense detail
- Users send reminder notifications
  - push, SMS, email (stretch)
- Open venmo (stretch)

### Week 4
Complete remaining core features, including ambiguous technical problems.
- My chore chart schedule
- Chore chart calendar
  - https://developers.google.com/calendar/api
- Add chore
- View chore detail


### Week 5
Final self review is due EOW. Add additional stretch features and polish.
- Push notifications (stretch)
- Streaks/rewards (stretch)
- Chore calendar multiple views (stretch)

### Week 6
Add additional stretch features and polish.
- Additional stretch features
- Improve styling

## Expectations
- [X] Difficult/Ambiguous Technical Problems
  - Connect to Google calendar using Google Calendar API
    - Add calendar to each user's Gmail account (requires each user to sign in with Google)
    - Add calendar to one Gmail premade account ([scaleable](https://support.google.com/a/answer/2905486?hl=en#:~:text=Follow%20use%20guidelines%20for%20paid%20accounts&text=Do%20not%20create%20more%20than%2060%20calendars%20in%20a%20short%20period.)?)
  - Unit testing (stretch)
  - Animation library (stretch)
    - Spark button
    - https://proandroiddev.com/complex-ui-animation-on-android-8f7a46f4aec4
  - Multiple methods of authentication --> determines what additional features they have access
- [X] User authentication
- [X] Your app interacts with a database (e.g. Parse)
- [X] Your app integrates with at least one SDK (e.g. Google Maps SDK, Facebook SDK) or API (that you didn’t learn about in CodePath)
  - API to generate random string for circle code
- [X] Your app uses at least one gesture (e.g. double tap to like, e.g. pinch to scale)
  - swipe to remove expense or chore
- [X] Your app incorporates at least one external library to add visual polish
  - Scanning (stretch), calendar (Cosmo calendar), animations
- [X] Your app uses at least one animation (e.g. fade in/out, e.g. animating a view growing and shrinking)
  - Between screens, after log in etc.
  - Adding expense, chore


## Wireframes
![IMG_0692](https://user-images.githubusercontent.com/31111505/173660646-8b7e29d1-139c-45e2-8445-57111de530d2.jpg)

## Schema
### Models
Relational database instead? - more queries but smaller queries

#### User

| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the user (default field) |
| email         | String   | user's email |
| phoneNumber   | String   | user's phone number |
| profileImage  | File     | image that user posts |
| points        | Number   | number of points accumulated from circle tasks |

#### Circle

| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the circle (default field) |
| name          | String   | name of circle |
| image         | File     | circle's image |

#### UserCircle
| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the UserCircle (default field) |
| user          | User     | pointer to user |
| circle        | Circle   | pointer to circle |


#### CompletedChore
| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the CompletedChore (default field) |
| user          | User     | pointer to user |
| chore         | Chore    | pointer to circle |

#### ChoreAssignment
| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the CompletedChore (default field) |
| user          | User     | pointer to user |
| chore         | Chore    | pointer to circle |

#### Chore

| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the chore (default field) |
| circle        | Circle   | circle's image |
| creator       | User     | user who made chore |
| title         | String   | name of chore |
| description   | String   | description of chore |
| priority      | Number   | priority of chore |
| points        | Number   | number of points user obtained from completing chore |
| dueDate       | Date     | date chore due |
| time          | String   | time of chore (if allDay is false) |

#### Custom Frequency
| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the custom frequency (default field) |
| chore         | Chore    | pointer to chore |
| startDate     | String   | date of first chore due date |
| endDate       | String   | date of last chore due date| 
| daysOfWeek    | List     | list of days of the week as Strings which chore repeats (if repeats true) |
| weekFrequency | Number   | chore repeats every how many weeks (if repeats true) |

#### Expense

| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the expense (default field) |
| total         | Number   | total amount paid |
| creator       | User     | user who created expense |
| title         | String   | name of expense |
| description   | String   | description of expense |
| proof         | File     | receipts etc. |

#### Transaction

| Property      | Type     | Description |
   | ------------- | -------- | ------------|
| objectId      | String   | unique id for the transaction (default field) |
| amount        | Number   | amount owed |
| receiver      | User     | user who is receiving amount |
| payer         | User     | user who is paying amount | 
| createdAt     | Date     | date transaction created |
| completed     | Boolean  | whether transaction has been paid |
| expense       | Expense  | pointer to related expense | 

![thumbnail_IMG_0693](https://user-images.githubusercontent.com/31111505/174542790-bbeff589-f180-4730-8612-79ca24f68f83.jpeg)

### Networking
#### List of network requests by screen
- Create Circle
  - (Create/POST) Create circle object
  - (Create/POST) Create CircleUser object
- Home Feed Screen
  - (Read/GET) Circle object
  - (Read/GET) User object
- Chores and Chores Detail Screens
  - (Create/POST, Read/GET) CompletedChore
  - (Read/GET) Read Chore info
  - (Read/GET) Read Chore Assignment
- Add Chores Screen
  - (Create/POST) Create new Chore
  - (Create/POST) ChoreAssignment
- Expense and Expense Detail Screens
  - (Read/Get, Update/PUT) Read and update a transaction
  - (Read/Get) Read expense info
- Add Expense
  - (Create/POST) Create a transaction
  - (Create/POST) Create an expense
- Settings
  - (Create/POST) Create circle object
  - (Update/PATCH) Update circle
  - (Update/PATCH) User object

#### [OPTIONAL:] Existing API Endpoints
