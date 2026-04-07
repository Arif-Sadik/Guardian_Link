package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for managing FAQs for Support and Donors
 */
public class FAQs {
    
    public static class FAQ {
        public final String title;
        public final String message;
        public final String description;
        public final String category;
        
        public FAQ(String title, String message, String description, String category) {
            this.title = title;
            this.message = message;
            this.description = description;
            this.category = category;
        }
    }
    
    public static final List<FAQ> DONOR_FAQS = Arrays.asList(
            new FAQ("How do I make a donation?", 
                    "You can make donations through your donor dashboard. Select a child to sponsor and enter the donation amount. We support multiple payment methods.",
                    "Visit your donor dashboard, select a child, and enter the donation amount. Multiple payment methods are available.",
                    "Donations"),
            new FAQ("What is the sponsorship program?", 
                    "Through our sponsorship program, you can directly sponsor a child's education and medical care. Your contributions directly impact their lives.",
                    "Directly sponsor a child's education and medical needs. Your contributions have a direct impact on their lives.",
                    "Sponsorship"),
            new FAQ("How often do I receive updates about the children I sponsor?", 
                    "You receive monthly updates about the child's progress, including education records, medical checkups, and general well-being status.",
                    "Monthly updates on education progress, medical checkups, and overall well-being status.",
                    "Updates"),
            new FAQ("Can I change how my donation is used?", 
                    "Yes! You can specify whether your donation should be used for education, medical care, or general welfare when making a donation.",
                    "Specify your donation purpose: education, medical care, or general welfare.",
                    "Donations"),
            new FAQ("Is my donation tax deductible?", 
                    "Yes, we are a registered non-profit organization. Please consult with your tax advisor or contact us for tax documentation.",
                    "We are a registered non-profit. Contact us for tax documentation.",
                    "Tax & Compliance"),
            new FAQ("How can I track my donations?", 
                    "Your donor dashboard shows a complete donation history with dates, amounts, and how each donation was utilized.",
                    "Access your complete donation history in the dashboard with dates, amounts, and usage details.",
                    "Contributions"),
            new FAQ("What security measures protect my payment information?", 
                    "We use industry-standard encryption and PCI compliance standards to protect all payment information.",
                    "We use industry-standard encryption and PCI compliance to protect your payment data.",
                    "Security"),
            new FAQ("Can I pause or stop my sponsorship?", 
                    "Yes, you can modify or stop your sponsorship at any time from your dashboard without penalties.",
                    "Modify or stop your sponsorship anytime from your dashboard without penalties.",
                    "Sponsorship")
    );
    
    public static final List<FAQ> CAREGIVER_FAQS = Arrays.asList(
            new FAQ("How do I submit medical records?", 
                    "Access your dashboard, navigate to the Medical Records section, and upload the required documents.",
                    "Go to the Medical Records section and upload required documents.",
                    "Records"),
            new FAQ("What documents are needed for education records?", 
                    "We need school reports, grade cards, and attendance certificates for the current academic year.",
                    "Submit school reports, grade cards, and current attendance certificates.",
                    "Records"),
            new FAQ("How do I request support for a child?", 
                    "Use the support request form on your dashboard to describe the issue. Our support team will respond within 24 hours.",
                    "Fill the support request form. Our team responds within 24 hours.",
                    "Support"),
            new FAQ("Can I reassign a child to another caregiver?", 
                    "Child reassignments must be approved by the organization admin. Please contact your admin directly.",
                    "Contact your organization admin for approval and child reassignment.",
                    "Child Management"),
            new FAQ("How do I report a medical emergency?", 
                    "There is an emergency alert button on your dashboard. Use it to immediately notify the support team and organization admin.",
                    "Use the emergency alert button on your dashboard to notify support team immediately.",
                    "Emergency"),
            new FAQ("What if I need to take leave from caregiving?", 
                    "Contact your organization admin at least 2 weeks in advance to arrange temporary coverage for your assigned children.",
                    "Contact your admin at least 2 weeks before to arrange coverage.",
                    "Leave")
    );
    
    public static List<FAQ> getFAQsByCategory(String category) {
        return DONOR_FAQS.stream()
                .filter(faq -> faq.category.equalsIgnoreCase(category))
                .toList();
    }
    
    public static List<String> getDonorCategories() {
        return DONOR_FAQS.stream()
                .map(faq -> faq.category)
                .distinct()
                .toList();
    }
}
