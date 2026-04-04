import { motion } from 'framer-motion'

export function PageContainer({ title, subtitle, children, actions }) {
  const MotionH1 = motion.h1
  const MotionDiv = motion.div
  return (
    <div className="cc-gradient">
      <div className="mx-auto max-w-6xl px-4 pb-16 pt-10 sm:px-6">
        {(title || subtitle || actions) && (
          <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
              {title ? (
                <MotionH1
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl"
                >
                  {title}
                </MotionH1>
              ) : null}
              {subtitle ? <div className="mt-2 max-w-2xl text-sm text-slate-600">{subtitle}</div> : null}
            </div>
            {actions ? <div className="flex items-center gap-2">{actions}</div> : null}
          </div>
        )}

        <MotionDiv initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.25 }}>
          {children}
        </MotionDiv>
      </div>
    </div>
  )
}
