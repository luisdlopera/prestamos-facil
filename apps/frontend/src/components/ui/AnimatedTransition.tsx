"use client";

import type { ReactNode } from "react";
import { motion } from "framer-motion";
import { pageTransition } from "@/lib/animations/variants";

interface AnimatedTransitionProps {
  children: ReactNode;
  className?: string;
}

export function AnimatedTransition({ children, className }: AnimatedTransitionProps) {
  return (
    <motion.div
      variants={pageTransition}
      initial="hidden"
      animate="visible"
      exit="exit"
      className={className}
    >
      {children}
    </motion.div>
  );
}
