package dasturlash.uz.event;
// Carry recipient details to the welcome-email listener.
public record ProfileActivatedEvent(String email, String name) {
}
