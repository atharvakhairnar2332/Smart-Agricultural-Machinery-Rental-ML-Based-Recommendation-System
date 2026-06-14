# Smart-Agricultural-Machinery-Rental-ML-Based-Recommendation-System
KhetSeva is a smart agricultural machinery rental platform built using Android (Jetpack Compose), FastAPI, MySQL, and Machine Learning. It enables farmers to receive machinery recommendations, browse available equipment, manage rental requests, and connect with machine owners through a unified digital ecosystem.


#KhetSeva - Smart Agricultural Machinery Rental & ML-Based Recommendation System

## Overview

KhetSeva is an Android-based Smart Agricultural Machinery Rental and Recommendation Platform designed to connect farmers with agricultural machinery owners through a digital ecosystem.

The system helps farmers discover suitable agricultural equipment based on farm requirements using Machine Learning models and enables seamless machinery rental management through a centralized platform.

The platform provides:

* Agricultural machinery recommendations using Machine Learning
* Machinery rental marketplace
* Machine listing and management
* Rental request tracking
* Notification management
* Image-based machinery listings
* Secure user authentication and profile management

---

## Problem Statement

Small and medium-scale farmers often face difficulties in accessing modern agricultural machinery due to high ownership costs and limited availability.

At the same time, many machinery owners possess underutilized equipment that remains idle during non-operational periods.

KhetSeva addresses this problem by:

* Connecting farmers with machinery owners
* Providing intelligent machinery recommendations
* Enabling machinery sharing and rental
* Improving machinery utilization
* Reducing operational costs for farmers

---

## Key Features

### User Management

* User Registration
* Secure Login
* Logout
* Change Password
* Edit Profile
* Password Hashing using bcrypt

### Machine Recommendation System

* Tractor Recommendation
* Harvester Recommendation
* Rotavator Recommendation
* Seed Drill Recommendation

Recommendations are generated using Machine Learning models based on:

* Farm Area
* Crop Type
* Soil Type
* Field Conditions
* Operational Requirements

### Machinery Marketplace

* Browse Available Machines
* View Machine Details
* View Owner Details
* Request Machine Rental
* Undo Rental Request

### Machine Management

* Add Machine
* Edit Machine
* Delete Machine
* Upload Machine Images
* Manage Rental Pricing

### Dashboard

* Recently Viewed Machines
* My Machines
* Incoming Requests
* Sent Requests
* Notifications
* Rented Machines

### Notification System

* Rental Request Notifications
* Request Status Updates
* Accepted Requests
* Rejected Requests

---

## System Architecture

The system follows a multi-layer architecture consisting of:

### Frontend

* Android
* Kotlin
* Jetpack Compose
* Retrofit
* Coil Image Loader
* SharedPreferences

### Backend

* Python
* FastAPI
* SQLAlchemy ORM

### Database

* MySQL

### Machine Learning

* Agricultural Machinery Recommendation Models

### Image Storage

* Local Server Image Storage

---

## Technology Stack

| Component            | Technology       |
| -------------------- | ---------------- |
| Mobile Application   | Android          |
| Programming Language | Kotlin           |
| UI Framework         | Jetpack Compose  |
| API Communication    | Retrofit         |
| Backend Framework    | FastAPI          |
| ORM                  | SQLAlchemy       |
| Database             | MySQL            |
| Password Security    | bcrypt           |
| Image Loading        | Coil             |
| Machine Learning     | Python ML Models |

---

## Database Schema

### Users

| Field    |
| -------- |
| id       |
| name     |
| phone    |
| password |
| country  |
| state    |
| city     |
| email    |

### Machines

| Field           |
| --------------- |
| id              |
| type            |
| model_name      |
| hp_range        |
| cutting_width   |
| working_width   |
| row_count       |
| price_per_hour  |
| price_per_day   |
| price_per_week  |
| price_per_month |
| owner_id        |
| image_url       |

### Machine Requests

| Field        |
| ------------ |
| id           |
| machine_id   |
| requester_id |
| owner_id     |
| status       |

---

## Recommendation Workflow

1. Farmer enters farm details.
2. Mobile application sends recommendation request to FastAPI backend.
3. Backend processes user inputs.
4. Appropriate Machine Learning model is selected.
5. Model generates recommended specifications.
6. Backend searches available machines matching the recommendation.
7. Ranked machinery recommendations are returned to the application.
8. Farmer views recommended machines and rental options.

---

## Rental Workflow

1. Machine owner lists machinery.
2. Machinery information is stored in the database.
3. Farmer browses and requests machinery.
4. Request is stored in Machine_Requests.
5. Owner receives notification.
6. Owner accepts or rejects request.
7. Farmer receives status update.

---

## Security Features

* Password Hashing using bcrypt
* Unique Email Validation
* Unique Phone Validation
* API Input Validation
* Secure Authentication Flow
* Session Persistence using SharedPreferences

