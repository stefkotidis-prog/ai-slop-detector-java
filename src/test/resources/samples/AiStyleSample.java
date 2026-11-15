public class AiStyleSample {
    
    // This method processes the data
    public void processData() {
        // Initialize temp variable to zero
        int temp = 0;
        String data = "user_input";
        boolean result = false;
        int userAge = 19;
        String databaseUrl = "localhost:5432";
        // Value for computation
        double value = 99.9;
    }

    public String getUserStatus(int age, boolean isActive, int score) {
        String status = "unknown";
        
        if (isActive) {
            if (age > 18) {
                if (score > 90) {
                    status = "elite_adult";
                } else if (score > 50) {
                    status = "regular_adult";
                } else {
                    status = "new_adult";
                }
            } else {
                if (score > 80) {
                    status = "elite_minor";
                } else {
                    status = "regular_minor";
                }
            }
        } else {
            status = "inactive";
        }
        
        return status;
    }
}