package com.GAKOM_ECOTACNA.ECOTACNA.service;

import com.GAKOM_ECOTACNA.ECOTACNA.dto.CompanySummaryResponse;
import com.GAKOM_ECOTACNA.ECOTACNA.exception.ResourceNotFoundException;
import com.GAKOM_ECOTACNA.ECOTACNA.model.Company;
import com.GAKOM_ECOTACNA.ECOTACNA.model.PickupRequestStatus;
import com.GAKOM_ECOTACNA.ECOTACNA.model.User;
import com.GAKOM_ECOTACNA.ECOTACNA.repository.CompanyRepository;
import com.GAKOM_ECOTACNA.ECOTACNA.repository.PickupRequestRepository;
import com.GAKOM_ECOTACNA.ECOTACNA.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.GAKOM_ECOTACNA.ECOTACNA.dto.CompanyGeneralDashboardResponse;
import com.GAKOM_ECOTACNA.ECOTACNA.model.PickupRequest;
import com.GAKOM_ECOTACNA.ECOTACNA.model.Subscription;
import com.GAKOM_ECOTACNA.ECOTACNA.model.SubscriptionPlan;
import com.GAKOM_ECOTACNA.ECOTACNA.model.TransportUnit;
import com.GAKOM_ECOTACNA.ECOTACNA.repository.SubscriptionRepository;

@Service
public class CompanyPortalService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PickupRequestRepository pickupRequestRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public CompanyPortalService(CompanyRepository companyRepository,
                                UserRepository userRepository,
                                PickupRequestRepository pickupRequestRepository,
                                SubscriptionRepository subscriptionRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.pickupRequestRepository = pickupRequestRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProfile(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada."));
        User user = userRepository.findByCompanyIdAndRole(companyId, com.GAKOM_ECOTACNA.ECOTACNA.model.Role.GENERADOR)
                .stream().findFirst()
                .or(() -> userRepository.findByCompanyId(companyId).stream().findFirst())
                .orElse(null);

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", company.getId());
        profile.put("ruc", company.getRuc());
        profile.put("razonSocial", company.getBusinessName());
        profile.put("direccion", company.getAddress());
        profile.put("tipoEmpresa", company.getCompanyType().name());

        if (user != null) {
            profile.put("correo", user.getEmail());
            profile.put("rol", user.getRole().name());
            String contactName = (user.getFirstName() != null ? user.getFirstName() : "") + 
                                 (user.getLastName() != null ? " " + user.getLastName() : "");
            profile.put("personaContacto", contactName.trim());
            profile.put("telefono", user.getPhone());
        }
        return profile;
    }

    @Transactional(readOnly = true)
    public CompanySummaryResponse getSummary(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada."));

        long totalRequests = pickupRequestRepository.findByCompanyIdOrderByRequestedAtDesc(companyId).size();

        CompanySummaryResponse summary = new CompanySummaryResponse();
        summary.setEmpresaId(companyId);
        summary.setRazonSocial(company.getBusinessName());
        summary.setTotalSolicitudes(totalRequests);
        summary.setTotalLitrosReciclados(BigDecimal.ZERO);
        summary.setSolicitudesPendientes(pickupRequestRepository.countByCompanyIdAndStatusIn(companyId,
                List.of(PickupRequestStatus.PENDIENTE, PickupRequestStatus.PROGRAMADO, PickupRequestStatus.EN_RUTA)));
        summary.setSolicitudesCompletadas(pickupRequestRepository.countByCompanyIdAndStatusIn(companyId,
                List.of(PickupRequestStatus.RECOGIDO, PickupRequestStatus.COMPLETADO)));
        return summary;
    }

    @Transactional
    public User updateContactData(Long userId, com.GAKOM_ECOTACNA.ECOTACNA.dto.ContactUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new com.GAKOM_ECOTACNA.ECOTACNA.exception.BusinessException("El correo electrónico ya está registrado por otro usuario.");
            }
        }

        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        String[] nameParts = request.getContactPerson().trim().split("\\s+", 2);
        user.setFirstName(nameParts[0]);
        if (nameParts.length > 1) {
            user.setLastName(nameParts[1]);
        } else {
            user.setLastName("");
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public CompanyGeneralDashboardResponse getDashboardGeneral(User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new ResourceNotFoundException("Empresa no encontrada.");
        }

        // --- Company Info ---
        String contactName = ((user.getFirstName() != null ? user.getFirstName() : "") +
                (user.getLastName() != null ? " " + user.getLastName() : "")).trim();
        CompanyGeneralDashboardResponse.CompanyInfo companyInfo = CompanyGeneralDashboardResponse.CompanyInfo.builder()
                .id(company.getId())
                .businessName(company.getBusinessName())
                .ruc(company.getRuc())
                .companyType(company.getCompanyType() != null ? company.getCompanyType().name() : "DESCONOCIDO")
                .status(company.getSubscriptionStatus() != null ? company.getSubscriptionStatus().name() : "DESCONOCIDO")
                .address(company.getAddress())
                .email(user.getEmail())
                .phone(user.getPhone())
                .contactPerson(contactName.isEmpty() ? null : contactName)
                .createdAt(company.getCreatedAt() != null ? company.getCreatedAt().toLocalDate().toString() : null)
                .build();

        // --- All requests ---
        List<PickupRequest> allRequests = pickupRequestRepository.findByCompanyIdOrderByRequestedAtDesc(company.getId());

        List<PickupRequest> completed = allRequests.stream()
                .filter(r -> r.getStatus() == PickupRequestStatus.COMPLETADO || r.getStatus() == PickupRequestStatus.RECOGIDO)
                .collect(Collectors.toList());

        long pendingCount = allRequests.stream()
                .filter(r -> r.getStatus() == PickupRequestStatus.PENDIENTE ||
                             r.getStatus() == PickupRequestStatus.PROGRAMADO ||
                             r.getStatus() == PickupRequestStatus.EN_RUTA)
                .count();

        BigDecimal totalLiters = completed.stream()
                .map(r -> r.getLitrosConfirmados() != null ? r.getLitrosConfirmados()
                        : (r.getActualVolumeLiters() != null ? r.getActualVolumeLiters() 
                        : (r.getApproximateVolumeLiters() != null ? r.getApproximateVolumeLiters() : BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = completed.stream()
                .map(r -> {
                    if (r.getMontoTotal() != null) return r.getMontoTotal();
                    BigDecimal liters = r.getLitrosConfirmados() != null ? r.getLitrosConfirmados() : BigDecimal.ZERO;
                    BigDecimal price = r.getPrecioPorLitro() != null ? r.getPrecioPorLitro() : BigDecimal.ZERO;
                    return liters.multiply(price);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // --- KPIs ---
        CompanyGeneralDashboardResponse.KpisInfo kpis = CompanyGeneralDashboardResponse.KpisInfo.builder()
                .totalLitersRecycled(totalLiters)
                .totalRequests(allRequests.size())
                .completedRequests(completed.size())
                .pendingRequests(pendingCount)
                .totalPaidToCollectors(totalPaid)
                .activeRequest(pendingCount > 0)
                .build();

        // --- Monthly Evolution (last 6 months) ---
        LocalDate now = LocalDate.now();
        List<CompanyGeneralDashboardResponse.MonthlyEvolutionItem> monthlyEvolution = java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> {
                    LocalDate monthDate = now.minusMonths(5 - i);
                    BigDecimal monthLiters = completed.stream()
                            .filter(r -> {
                                java.time.LocalDateTime dateToUse = r.getScheduledAt() != null ? r.getScheduledAt() : r.getRequestedAt();
                                return dateToUse != null &&
                                        dateToUse.getYear() == monthDate.getYear() &&
                                        dateToUse.getMonth() == monthDate.getMonth();
                            })
                            .map(r -> r.getLitrosConfirmados() != null ? r.getLitrosConfirmados()
                                    : (r.getActualVolumeLiters() != null ? r.getActualVolumeLiters() 
                                    : (r.getApproximateVolumeLiters() != null ? r.getApproximateVolumeLiters() : BigDecimal.ZERO)))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                            
                    // Spanish month names
                    String[] monthNames = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                    String monthName = monthNames[monthDate.getMonthValue() - 1];
                    
                    return CompanyGeneralDashboardResponse.MonthlyEvolutionItem.builder()
                            .month(monthName)
                            .liters(monthLiters)
                            .build();
                })
                .collect(Collectors.toList());

        // --- Operational Summary ---
        CompanyGeneralDashboardResponse.OperationalSummary opSummary = CompanyGeneralDashboardResponse.OperationalSummary.builder()
                .type(company.getCompanyType() != null ? company.getCompanyType().name() : "DESCONOCIDO")
                .ruc(company.getRuc())
                .address(company.getAddress())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(company.getSubscriptionStatus() != null ? company.getSubscriptionStatus().name() : "DESCONOCIDO")
                .memberSince(company.getCreatedAt() != null ? company.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null)
                .build();

        // --- Request Distribution ---
        Map<PickupRequestStatus, List<PickupRequest>> byStatus = allRequests.stream()
                .collect(Collectors.groupingBy(PickupRequest::getStatus));
                
        List<CompanyGeneralDashboardResponse.RequestDistributionItem> requestDistribution = byStatus.entrySet().stream()
                .map(e -> {
                    BigDecimal liters = e.getValue().stream()
                            .map(r -> r.getLitrosConfirmados() != null ? r.getLitrosConfirmados()
                                    : (r.getActualVolumeLiters() != null ? r.getActualVolumeLiters()
                                    : (r.getApproximateVolumeLiters() != null ? r.getApproximateVolumeLiters() : BigDecimal.ZERO)))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return CompanyGeneralDashboardResponse.RequestDistributionItem.builder()
                            .label(e.getKey().name())
                            .value(e.getValue().size())
                            .liters(liters)
                            .build();
                })
                .collect(Collectors.toList());

        // --- Recent Requests ---
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
        List<CompanyGeneralDashboardResponse.RecentRequestItem> recentRequests = allRequests.stream()
                .limit(5)
                .map(r -> {
                    TransportUnit tu = r.getTransportUnit();
                    return CompanyGeneralDashboardResponse.RecentRequestItem.builder()
                            .id(r.getId())
                            .requestDate(r.getRequestedAt() != null ? r.getRequestedAt().format(dtf) : null)
                            .scheduledDate(r.getScheduledAt() != null ? r.getScheduledAt().format(dtf) : "No programada")
                            .liters(r.getLitrosConfirmados() != null ? r.getLitrosConfirmados()
                                    : (r.getActualVolumeLiters() != null ? r.getActualVolumeLiters()
                                    : r.getApproximateVolumeLiters()))
                            .pricePerLiter(r.getPrecioOfertadoPorLitro())
                            .totalAmount(r.getMontoTotal())
                            .status(r.getStatus().name())
                            .paymentStatus(r.getEstadoPago() != null ? r.getEstadoPago() : "PENDIENTE")
                            .collectorName(r.getCollectorUserId() != null ? getCollectorCompanyName(r.getCollectorUserId()) : "Sin recolector asignado")
                            .collectorRuc(r.getCollectorUserId() != null ? getCollectorCompanyRuc(r.getCollectorUserId()) : null)
                            .vehiclePlate(tu != null ? tu.getPlate() : "Vehículo no registrado")
                            .pickupAddress(r.getDireccion())
                            .build();
                })
                .collect(Collectors.toList());

        // --- Subscription ---
        CompanyGeneralDashboardResponse.SubscriptionInfo subInfo = buildSubscriptionInfo(company);

        return CompanyGeneralDashboardResponse.builder()
                .company(companyInfo)
                .kpis(kpis)
                .monthlyEvolution(monthlyEvolution)
                .operationalSummary(opSummary)
                .requestDistribution(requestDistribution)
                .recentRequests(recentRequests)
                .subscription(subInfo)
                .build();
    }
    
    private String getCollectorCompanyName(Long collectorUserId) {
        return userRepository.findById(collectorUserId)
                .map(User::getCompany)
                .map(Company::getBusinessName)
                .orElse("Desconocido");
    }

    private String getCollectorCompanyRuc(Long collectorUserId) {
        return userRepository.findById(collectorUserId)
                .map(User::getCompany)
                .map(Company::getRuc)
                .orElse("Desconocido");
    }

    private CompanyGeneralDashboardResponse.SubscriptionInfo buildSubscriptionInfo(Company company) {
        Optional<Subscription> subOpt = subscriptionRepository.findTopByCompanyIdOrderByCreatedAtDesc(company.getId());

        if (subOpt.isEmpty()) {
            return CompanyGeneralDashboardResponse.SubscriptionInfo.builder()
                    .planName("Sin suscripción")
                    .monthlyAmount(new BigDecimal("29.90"))
                    .status("SIN_SUSCRIPCION")
                    .daysRemaining(0)
                    .cancellationScheduled(false)
                    .message("No tienes una suscripción activa.")
                    .build();
        }

        Subscription sub = subOpt.get();
        SubscriptionPlan plan = sub.getPlan();

        LocalDate startDate = sub.getCurrentPeriodStart() != null ? sub.getCurrentPeriodStart() : (sub.getStartDate() != null ? sub.getStartDate() : null);
        LocalDate endDate = sub.getCurrentPeriodEnd() != null ? sub.getCurrentPeriodEnd() : null;
        LocalDate trialEndDate = sub.getTrialEndsAt();
        
        long daysRemaining = 0;
        if (endDate != null) {
            daysRemaining = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), endDate));
        }

        boolean cancelScheduled = sub.getScheduledCancellation() != null && sub.getScheduledCancellation();

        String message;
        String statusStr = sub.getStatus() != null ? sub.getStatus().name() : "DESCONOCIDO";
        if (cancelScheduled && endDate != null) {
            message = "Tu cancelación fue programada. Mantendrás acceso hasta el " + 
                      String.format("%02d/%02d/%d", endDate.getDayOfMonth(), endDate.getMonthValue(), endDate.getYear()) + ".";
        } else if ("ACTIVA".equals(statusStr)) {
            message = "Tu suscripción está activa.";
        } else if ("TRIAL".equals(statusStr) || "PRUEBA_GRATUITA".equals(statusStr) || "PRUEBA".equals(statusStr)) {
            if (trialEndDate != null) {
                message = String.format("Tu prueba gratuita vence el %02d/%02d/%d. Luego se aplicará el plan de S/ %.2f mensual.",
                    trialEndDate.getDayOfMonth(), trialEndDate.getMonthValue(), trialEndDate.getYear(),
                    plan != null ? plan.getMonthlyAmount() : 29.90);
            } else {
                message = "Estás en período de prueba gratuita.";
            }
        } else {
            message = "Estado de suscripción: " + statusStr;
        }

        return CompanyGeneralDashboardResponse.SubscriptionInfo.builder()
                .planName(plan != null ? plan.getName() : "Plan Generador Básico")
                .monthlyAmount(plan != null ? plan.getMonthlyAmount() : new BigDecimal("29.90"))
                .status(statusStr)
                .startDate(startDate)
                .trialEndDate(trialEndDate)
                .endDate(endDate)
                .daysRemaining(daysRemaining)
                .cancellationScheduled(cancelScheduled)
                .message(message)
                .build();
    }
}
