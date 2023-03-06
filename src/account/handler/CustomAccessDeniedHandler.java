package account.handler;

import account.entity.SecurityEvent;
import account.repository.SecurityEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Autowired
    private SecurityEventRepository securityEventRepository;
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException
    {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        String errorMessage = "Access Denied!";
        String errorType = "Forbidden";
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String path = request.getRequestURI();

        securityEventRepository.save(
                SecurityEvent.builder()
                        .date(LocalDate.now())
                        .action(SecurityEvent.EVENT.ACCESS_DENIED.toString())
                        .subject(username)
                        .object(path)
                        .path(path)
                        .build()
        );
        String json = String.format("{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                dateTimeFormatter.format(timestamp),
                HttpStatus.FORBIDDEN.value(),
                errorType,
                errorMessage,
                path);

        response.getWriter().write(json);
    }
}