import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { AnimatePresence, motion } from 'framer-motion'
import { Navbar } from './components/layout/Navbar'
import { ProtectedRoute } from './components/layout/ProtectedRoute'

import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { HomePage } from './pages/HomePage'
import { QuestionsPage } from './pages/QuestionsPage'
import { QuestionDetailPage } from './pages/QuestionDetailPage'
import { AskQuestionPage } from './pages/AskQuestionPage'
import { LostFoundPage } from './pages/LostFoundPage'
import { CreateLostFoundPage } from './pages/CreateLostFoundPage'
import { LostFoundDetailPage } from './pages/LostFoundDetailPage'
import { AdminDashboardPage } from './pages/AdminDashboardPage'
import { AdminUsersPage } from './pages/AdminUsersPage'
import { AdminQuestionsPage } from './pages/AdminQuestionsPage'
import { AdminQuestionDetailPage } from './pages/AdminQuestionDetailPage'
import { AdminLostFoundPage } from './pages/AdminLostFoundPage'
import { AdminClaimsPage } from './pages/AdminClaimsPage'

function App() {
  const location = useLocation()
  const MotionDiv = motion.div

  return (
    <div className="min-h-screen">
      <Navbar />
      <AnimatePresence mode="wait" initial={false}>
        <MotionDiv
          key={location.pathname}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -6 }}
          transition={{ duration: 0.2 }}
        >
          <Routes location={location}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <HomePage />
                </ProtectedRoute>
              }
            />

            <Route path="/questions" element={<QuestionsPage />} />
            <Route path="/questions/:id" element={<QuestionDetailPage />} />

            <Route
              path="/ask"
              element={
                <ProtectedRoute>
                  <AskQuestionPage />
                </ProtectedRoute>
              }
            />

            <Route path="/lost-found" element={<LostFoundPage />} />
            <Route path="/lost-found/:id" element={<LostFoundDetailPage />} />
            <Route
              path="/lost-found/new"
              element={
                <ProtectedRoute>
                  <CreateLostFoundPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/admin"
              element={
                <ProtectedRoute requireAdmin>
                  <AdminDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/users"
              element={
                <ProtectedRoute requireAdmin>
                  <AdminUsersPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/questions"
              element={
                <ProtectedRoute requireAdmin>
                  <AdminQuestionsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/questions/:id"
              element={
                <ProtectedRoute requireAdmin>
                  <AdminQuestionDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/lost-found"
              element={
                <ProtectedRoute requireAdmin>
                  <AdminLostFoundPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/claims"
              element={
                <ProtectedRoute requireAdmin>
                  <AdminClaimsPage />
                </ProtectedRoute>
              }
            />

            <Route path="*" element={<Navigate to="/questions" replace />} />
          </Routes>
        </MotionDiv>
      </AnimatePresence>
    </div>
  )
}

export default App
