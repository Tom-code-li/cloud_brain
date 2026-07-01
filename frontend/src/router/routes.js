import HisLayout from '../layouts/HisLayout.vue';
import LoginView from '../views/LoginView.vue';
import PatientView from '../views/PatientView.vue';
import MedicalHomeView from '../views/MedicalHomeView.vue';
import ExamRequestView from '../views/ExamRequestView.vue';
import ExamListView from '../views/ExamListView.vue';
import ExamDetailView from '../views/ExamDetailView.vue';
import LabRequestView from '../views/LabRequestView.vue';
import LabListView from '../views/LabListView.vue';
import LabDetailView from '../views/LabDetailView.vue';
import ConsultRecordView from '../views/ConsultRecordView.vue';
import AiReportView from '../views/AiReportView.vue';
import AiReportDetailView from '../views/AiReportDetailView.vue';
import DiagnosisView from '../views/DiagnosisView.vue';
import DiagnosisDetailView from '../views/DiagnosisDetailView.vue';
import PrescriptionView from '../views/PrescriptionView.vue';
import PrescriptionDetailView from '../views/PrescriptionDetailView.vue';
import FeeQueryView from '../views/FeeQueryView.vue';
import DispatchManagement from '../views/pharmacy/DispatchManagement.vue';
import PharmacyRefundManagement from '../views/pharmacy/RefundManagement.vue';
import StockManagement from '../views/pharmacy/StockManagement.vue';
import RegistrationDashboard from '../views/registration/RegistrationDashboard.vue';
import OfflineRegistration from '../views/registration/OfflineRegistration.vue';
import OnlineRegistrationConfirm from '../views/registration/OnlineRegistrationConfirm.vue';
import FeeManagement from '../views/registration/FeeManagement.vue';
import FeeManagementDetail from '../views/registration/FeeManagementDetail.vue';
import RegistrationFeeQuery from '../views/registration/FeeQuery.vue';
import RefundManagement from '../views/registration/RefundManagement.vue';

export const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true }
  },
  {
    path: '/',
    component: HisLayout,
    children: [
      { path: '', redirect: '/patients' },
      { path: 'patients', name: 'patient-view', component: PatientView, meta: { menuKey: 'patient-view', role: 'outpatient' } },
      { path: 'medical-home', name: 'medical-home', component: MedicalHomeView, meta: { menuKey: 'medical-home', role: 'outpatient' } },
      { path: 'exam-request', name: 'exam-req', component: ExamRequestView, meta: { menuKey: 'exam-req', role: 'outpatient' } },
      { path: 'lab-request', name: 'lab-req', component: LabRequestView, meta: { menuKey: 'lab-req', role: 'outpatient' } },
      { path: 'consult-record', name: 'consult-record', component: ConsultRecordView, meta: { menuKey: 'consult-record', role: 'outpatient' } },
      { path: 'ai-report', name: 'ai-report', component: AiReportView, meta: { menuKey: 'ai-report', role: 'outpatient' } },
      { path: 'ai-report-detail', name: 'ai-report-detail', component: AiReportDetailView, meta: { menuKey: 'ai-report', role: 'outpatient' } },
      { path: 'diagnosis', name: 'diagnosis', component: DiagnosisView, meta: { menuKey: 'diagnosis', role: 'outpatient' } },
      { path: 'diagnosis-detail', name: 'diagnosis-detail', component: DiagnosisDetailView, meta: { menuKey: 'diagnosis', role: 'outpatient' } },
      { path: 'prescription', name: 'prescription', component: PrescriptionView, meta: { menuKey: 'prescription', role: 'outpatient' } },
      { path: 'prescription-detail', name: 'prescription-detail', component: PrescriptionDetailView, meta: { menuKey: 'prescription', role: 'outpatient' } },
      { path: 'fee-query', name: 'fee-query', component: FeeQueryView, meta: { menuKey: 'fee-query', role: 'outpatient' } },
      { path: 'exam', name: 'exam-workbench', component: ExamListView, meta: { menuKey: 'exam-workbench', role: 'exam' } },
      { path: 'exam/:orderItemId', name: 'exam-detail', component: ExamDetailView, meta: { menuKey: 'exam-workbench', role: 'exam' } },
      { path: 'lab', name: 'lab-workbench', component: LabListView, meta: { menuKey: 'lab-workbench', role: 'lab' } },
      { path: 'lab/:orderItemId', name: 'lab-detail', component: LabDetailView, meta: { menuKey: 'lab-workbench', role: 'lab' } },
      { path: 'registration', redirect: '/registration/dashboard' },
      {
        path: 'registration/dashboard',
        name: 'registration-dashboard',
        component: RegistrationDashboard,
        meta: { menuKey: 'registration-dashboard', role: 'registration' }
      },
      {
        path: 'registration/offline',
        name: 'offline-registration',
        component: OfflineRegistration,
        meta: { menuKey: 'offline-registration', role: 'registration' }
      },
      {
        path: 'registration/online-confirm',
        name: 'online-registration-confirm',
        component: OnlineRegistrationConfirm,
        meta: { menuKey: 'online-registration-confirm', role: 'registration' }
      },
      {
        path: 'registration/fee-management',
        name: 'registration-fee-management',
        component: FeeManagement,
        meta: { menuKey: 'registration-fee-management', role: 'registration' }
      },
      {
        path: 'registration/fee-management/:patientId',
        name: 'registration-fee-management-detail',
        component: FeeManagementDetail,
        meta: { menuKey: 'registration-fee-management', role: 'registration' }
      },
      {
        path: 'registration/fee-query',
        name: 'registration-fee-query',
        component: RegistrationFeeQuery,
        meta: { menuKey: 'registration-fee-query', role: 'registration' }
      },
      {
        path: 'registration/refund',
        name: 'registration-refund-management',
        component: RefundManagement,
        meta: { menuKey: 'registration-refund-management', role: 'registration' }
      },
      {
        path: 'pharmacy/dispatch',
        name: 'pharmacy-dispatch',
        component: DispatchManagement,
        meta: { menuKey: 'pharmacy-dispatch', role: 'pharmacy' }
      },
      {
        path: 'pharmacy/refund',
        name: 'pharmacy-refund',
        component: PharmacyRefundManagement,
        meta: { menuKey: 'pharmacy-refund', role: 'pharmacy' }
      },
      {
        path: 'pharmacy/stock',
        name: 'pharmacy-stock',
        component: StockManagement,
        meta: { menuKey: 'pharmacy-stock', role: 'pharmacy' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];
