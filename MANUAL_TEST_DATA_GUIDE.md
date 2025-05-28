# 📊 Manual Test Data Creation Guide

## 🎯 **Target User ID**: `1rHqzlFFdId0eaWqBohXIs4ex9u2`

## 📱 **Method 1: Using Firebase Console (Recommended)**

### **Step 1: Access Firebase Console**
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your ElektroniCare project
3. Navigate to **Firestore Database**

### **Step 2: Create User Profile**
1. Click **Start collection** → Enter `users`
2. Document ID: `1rHqzlFFdId0eaWqBohXIs4ex9u2`
3. Add these fields:

```json
{
  "userId": "1rHqzlFFdId0eaWqBohXIs4ex9u2",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+6281234567890",
  "address": "Jl. Teknologi No. 123, Jakarta Selatan",
  "joinDate": "2024-01-15T10:00:00Z",
  "lastLogin": "2024-05-28T15:30:00Z",
  "totalBookings": 5,
  "completedBookings": 3,
  "averageRating": 4.7,
  "loyaltyPoints": 350,
  "profileImageUrl": "https://res.cloudinary.com/elektronicare/image/upload/v1/profile_images/john_doe.jpg"
}
```

### **Step 3: Create Repair/Booking History**
1. Create collection: `repairs`
2. Add these 5 sample bookings:

#### **Booking 1: Completed Smartphone Repair**
Document ID: `EC202405ABC12345`
```json
{
  "userId": "1rHqzlFFdId0eaWqBohXIs4ex9u2",
  "serviceId": "smartphone_repair",
  "serviceName": "Smartphone Repair",
  "issueDescription": "Screen cracked after dropping the device. Touch functionality partially working.",
  "status": "completed",
  "estimatedCost": 150000,
  "actualCost": 150000,
  "appointmentTimestamp": "2024-05-20T10:00:00Z",
  "createdAt": "2024-05-18T14:30:00Z",
  "completedAt": "2024-05-21T16:00:00Z",
  "updatedAt": "2024-05-21T16:00:00Z",
  "location": "ElektroniCare Service Center",
  "technicianEmail": "agusseptiawanasep@gmail.com",
  "technicianName": "Agus Septiawan",
  "technicianNotes": "Replaced LCD screen and digitizer. Device fully functional.",
  "customerRating": 5,
  "paymentStatus": "paid",
  "paymentMethod": "cash",
  "warrantyExpiry": "2024-06-21T16:00:00Z",
  "deviceImageUrl": "https://res.cloudinary.com/elektronicare/image/upload/v1/repair_images/smartphone_crack.jpg"
}
```

#### **Booking 2: In Progress Laptop Repair**
Document ID: `EC202405DEF67890`
```json
{
  "userId": "1rHqzlFFdId0eaWqBohXIs4ex9u2",
  "serviceId": "laptop_repair",
  "serviceName": "Laptop Repair",
  "issueDescription": "Battery drains very quickly, device shuts down unexpectedly.",
  "status": "in_progress",
  "estimatedCost": 300000,
  "appointmentTimestamp": "2024-05-25T14:00:00Z",
  "createdAt": "2024-05-23T09:15:00Z",
  "updatedAt": "2024-05-26T11:30:00Z",
  "location": "ElektroniCare Service Center",
  "technicianEmail": "agusseptiawanasep@gmail.com",
  "technicianName": "Agus Septiawan",
  "technicianNotes": "Battery replacement in progress. Estimated completion: 2 days.",
  "paymentStatus": "pending"
}
```

#### **Booking 3: Pending Confirmation**
Document ID: `EC202405GHI11111`
```json
{
  "userId": "1rHqzlFFdId0eaWqBohXIs4ex9u2",
  "serviceId": "tablet_repair",
  "serviceName": "Tablet Repair",
  "issueDescription": "Device won't turn on, no response to power button.",
  "status": "pending_confirmation",
  "estimatedCost": 200000,
  "appointmentTimestamp": "2024-05-30T11:00:00Z",
  "createdAt": "2024-05-27T16:45:00Z",
  "updatedAt": "2024-05-27T16:45:00Z",
  "location": "ElektroniCare Service Center",
  "technicianEmail": "agusseptiawanasep@gmail.com",
  "technicianName": "Agus Septiawan",
  "technicianNotes": "Initial diagnosis pending. Will contact customer for confirmation.",
  "paymentStatus": "pending"
}
```

#### **Booking 4: Completed Headphone Repair**
Document ID: `EC202404JKL22222`
```json
{
  "userId": "1rHqzlFFdId0eaWqBohXIs4ex9u2",
  "serviceId": "headphone_repair",
  "serviceName": "Headphone Repair",
  "issueDescription": "Audio jack not working, no sound through headphones.",
  "status": "completed",
  "estimatedCost": 75000,
  "actualCost": 75000,
  "appointmentTimestamp": "2024-04-15T13:00:00Z",
  "createdAt": "2024-04-12T10:20:00Z",
  "completedAt": "2024-04-16T15:30:00Z",
  "updatedAt": "2024-04-16T15:30:00Z",
  "location": "ElektroniCare Service Center",
  "technicianEmail": "agusseptiawanasep@gmail.com",
  "technicianName": "Agus Septiawan",
  "technicianNotes": "Cleaned audio jack and replaced internal connector.",
  "customerRating": 4,
  "paymentStatus": "paid",
  "paymentMethod": "transfer",
  "warrantyExpiry": "2024-05-16T15:30:00Z"
}
```

#### **Booking 5: Cancelled Smartwatch Repair**
Document ID: `EC202403MNO33333`
```json
{
  "userId": "1rHqzlFFdId0eaWqBohXIs4ex9u2",
  "serviceId": "smartwatch_repair",
  "serviceName": "Smartwatch Repair",
  "issueDescription": "Overheating issues during heavy usage and charging.",
  "status": "cancelled",
  "estimatedCost": 100000,
  "appointmentTimestamp": "2024-03-20T09:00:00Z",
  "createdAt": "2024-03-18T14:10:00Z",
  "cancelledAt": "2024-03-19T12:00:00Z",
  "updatedAt": "2024-03-19T12:00:00Z",
  "location": "ElektroniCare Service Center",
  "technicianEmail": "agusseptiawanasep@gmail.com",
  "technicianName": "Agus Septiawan",
  "technicianNotes": "Customer cancelled due to cost concerns.",
  "cancellationReason": "Customer request",
  "paymentStatus": "cancelled"
}
```

---

## 📱 **Method 2: Using App Testing**

### **Step 1: Login with Test User**
1. Build and run the app
2. Login/register with email: `john.doe@example.com`
3. Complete profile setup

### **Step 2: Create Real Bookings**
1. Go to Services → Select a service
2. Fill booking form with test data
3. Upload sample device images
4. Submit booking
5. Repeat for different services and statuses

---

## 🔔 **Method 3: Add Notifications (Optional)**

Create collection: `notifications`

### **Sample Notification 1**
Document ID: `notif_001`
```json
{
  "userId": "1rHqzlFFdId0eaWqBohXIs4ex9u2",
  "title": "Booking Confirmed",
  "message": "Your smartphone repair booking has been confirmed for tomorrow at 10:00 AM",
  "type": "booking_confirmation",
  "timestamp": "2024-05-27T18:00:00Z",
  "read": false,
  "actionUrl": "/booking/details"
}
```

### **Sample Notification 2**
Document ID: `notif_002`
```json
{
  "userId": "1rHqzlFFdId0eaWqBohXIs4ex9u2",
  "title": "Repair Completed",
  "message": "Great news! Your laptop repair has been completed. You can pick it up anytime.",
  "type": "repair_completed",
  "timestamp": "2024-05-26T14:30:00Z",
  "read": true,
  "actionUrl": "/booking/completed"
}
```

---

## ✅ **Verification Steps**

### **1. Check Data in Firebase Console**
- Go to Firestore Database
- Verify `users` collection has the test user
- Verify `repairs` collection has 5 bookings
- Check all field types are correct

### **2. Test in App**
1. Login with test user
2. Go to History section
3. Verify all 5 bookings appear
4. Check different status filters work
5. Test booking details view

### **3. Test Features**
- ✅ View booking history
- ✅ Filter by status
- ✅ View booking details
- ✅ WhatsApp integration
- ✅ Email notifications

---

## 🎯 **Expected Results**

After creating this test data, the user `1rHqzlFFdId0eaWqBohXIs4ex9u2` will have:

- **5 Total Bookings**:
  - 2 Completed (with ratings)
  - 1 In Progress
  - 1 Pending Confirmation
  - 1 Cancelled

- **Complete Profile** with contact information
- **Realistic Timeline** spanning 3 months
- **Various Service Types** (smartphone, laptop, tablet, etc.)
- **Different Payment Status** (paid, pending, cancelled)
- **Technician Notes** for each booking
- **Device Images** for some bookings

This will provide comprehensive testing data for all app features!