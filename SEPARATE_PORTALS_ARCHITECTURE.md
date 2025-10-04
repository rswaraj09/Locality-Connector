# Separate Portals Architecture - Locality Connector

## 🏗️ Architecture Overview

This project now supports **separate websites for business and user login** while sharing the same MongoDB database. This allows for:

- **Different branding and user experience** for each portal
- **Shared data** between user and business dashboards
- **Cross-dashboard functionality** where users see business data and businesses see user orders

## 🚀 Portal Structure

### Main Entry Points

1. **Main Landing Page** (`/`)
   - Choose between User Portal or Business Portal
   - Updated with portal selection buttons

2. **User Portal** (`/user`)
   - Dedicated user entry point
   - User-specific branding (blue theme)
   - Links to user login/signup

3. **Business Portal** (`/business`)
   - Dedicated business entry point
   - Business-specific branding (pink theme)
   - Links to business login/signup

### Authentication Flow

#### User Flow:
```
/ → /user → /user/login → /enhanced-user-dashboard
```

#### Business Flow:
```
/ → /business → /business/login → /enhanced-business-dashboard
```

## 📊 Database Schema

### Shared Collections:
- **`users`** - User accounts
- **`businesses`** - Business accounts
- **`items`** - Products/services offered by businesses
- **`orders`** - Links users and businesses (NEW)
- **`feedback`** - Customer reviews and ratings (NEW)

### New Models:

#### Order Model
```java
@Document(collection = "orders")
public class Order {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String businessId;
    private String businessName;
    private List<OrderItem> items;
    private Double totalAmount;
    private String status; // PENDING, CONFIRMED, DELIVERED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Feedback Model
```java
@Document(collection = "feedback")
public class Feedback {
    private String id;
    private String userId;
    private String businessId;
    private String orderId;
    private Integer rating; // 1-5 stars
    private String comment;
    private LocalDateTime createdAt;
}
```

## 🎯 Cross-Dashboard Features

### User Dashboard Features:
- **View Local Businesses** - Browse all registered businesses
- **Filter by Category** - Food, Pharmacy, Clothing, etc.
- **View Business Items** - See products/services offered
- **Order History** - Track past orders
- **Quick Actions** - Find nearby shops, food, pharmacy, hospitals

### Business Dashboard Features:
- **Order Management** - View and manage customer orders
- **Customer Feedback** - See reviews and ratings
- **Order Statistics** - Total orders, pending orders, customer count
- **Customer Management** - View customer information
- **Quick Actions** - Manage listings, add items, update profile

## 🔧 API Endpoints

### User Dashboard APIs:
```
GET /api/user/dashboard/businesses - Get all businesses
GET /api/user/dashboard/businesses/category/{category} - Filter by category
GET /api/user/dashboard/items/business/{businessId} - Get business items
GET /api/user/dashboard/orders/{userId} - Get user orders
```

### Business Dashboard APIs:
```
GET /api/business/dashboard/orders - Get business orders
GET /api/business/dashboard/orders/status/{status} - Filter by status
GET /api/business/dashboard/feedback - Get customer feedback
GET /api/business/dashboard/customers - Get customer list
PUT /api/business/dashboard/orders/{orderId}/status - Update order status
```

## 🎨 UI/UX Features

### Separate Branding:
- **User Portal**: Blue gradient theme (#667eea to #764ba2)
- **Business Portal**: Pink gradient theme (#f093fb to #f5576c)
- **Consistent Design Language** across each portal
- **Responsive Design** for all screen sizes

### Enhanced Dashboards:
- **Real-time Data** - Live updates from shared database
- **Interactive Elements** - Click to view details, filter, sort
- **Status Management** - Update order statuses, view feedback
- **Statistics** - Visual stats for business performance

## 🚀 How to Use

### For Users:
1. Go to `/` (main page)
2. Click "User Portal"
3. Sign up or login
4. Browse businesses and their items
5. Place orders and track them

### For Businesses:
1. Go to `/` (main page)
2. Click "Business Portal"
3. Sign up or login
4. Manage listings and items
5. View and manage customer orders
6. Track customer feedback

## 🔄 Data Flow

### User → Business Data:
- Users can see all business listings
- Users can view business items/products
- Users can place orders with businesses

### Business → User Data:
- Businesses can see customer orders
- Businesses can view customer feedback
- Businesses can track customer information

## 🛠️ Technical Implementation

### Controllers Added:
- `UserDashboardController` - Handles user dashboard APIs
- `BusinessDashboardController` - Handles business dashboard APIs

### Services Added:
- `OrderService` - Manages order operations
- `FeedbackService` - Manages feedback operations

### Repositories Added:
- `OrderRepository` - Database operations for orders
- `FeedbackRepository` - Database operations for feedback

## 📱 Responsive Design

All templates are fully responsive and work on:
- Desktop computers
- Tablets
- Mobile phones

## 🔐 Security Features

- **Session Management** - Secure session handling
- **Role-based Access** - Different access levels for users vs businesses
- **Data Validation** - Input validation on all forms
- **Error Handling** - Graceful error handling throughout

## 🎯 Benefits of This Architecture

1. **Separation of Concerns** - Clear distinction between user and business interfaces
2. **Shared Data** - Single source of truth in MongoDB
3. **Scalability** - Easy to add new features to either portal
4. **User Experience** - Tailored experience for each user type
5. **Maintainability** - Clean separation makes code easier to maintain

## 🚀 Future Enhancements

- **Real-time Notifications** - WebSocket integration for live updates
- **Payment Integration** - Stripe/PayPal integration for orders
- **Advanced Analytics** - Business performance metrics
- **Mobile Apps** - Native mobile applications
- **API Documentation** - Swagger/OpenAPI documentation

This architecture provides a solid foundation for a marketplace platform where users and businesses can interact while maintaining separate, tailored experiences.

