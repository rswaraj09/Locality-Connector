# Environment Setup Guide

## MapTiler API Integration

### 1. Get MapTiler API Key

1. Visit [MapTiler Cloud](https://cloud.maptiler.com/)
2. Sign up for a free account
3. Go to your dashboard and create a new API key
4. Copy the API key

### 2. Configure Environment Variables

Create a `.env` file in the project root with the following content:

```env
# MapTiler API Configuration
MAPTILER_API_KEY=your_actual_api_key_here

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
```

### 3. Alternative: Set System Environment Variable

You can also set the environment variable directly:

**Windows:**
```cmd
set MAPTILER_API_KEY=your_actual_api_key_here
```

**Linux/Mac:**
```bash
export MAPTILER_API_KEY=your_actual_api_key_here
```

### 4. Features Available

- **Directions**: Get turn-by-turn directions from user location to businesses
- **Map Images**: Display static map images
- **Routing**: Calculate optimal routes
- **Geocoding**: Convert addresses to coordinates

### 5. Usage

The directions feature will automatically:
- Use the user's current location as the starting point
- Calculate routes to selected businesses
- Open directions in a new tab with MapTiler's routing service

### 6. API Limits

- Free tier includes 100,000 requests per month
- Rate limiting applies to prevent abuse
- For production use, consider upgrading to a paid plan

### 7. Testing

1. Start the application: `mvn spring-boot:run`
2. Navigate to the user dashboard
3. Click "Get Directions" on any business
4. The system will open MapTiler's routing interface in a new tab



