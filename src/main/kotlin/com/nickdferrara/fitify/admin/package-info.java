/**
 * The Admin module provides platform-wide administrative capabilities across three concerns:
 *
 * <ul>
 *   <li><b>Class & Schedule Management</b> — Creating, updating, and cancelling fitness classes
 *       and recurring schedules, with validations for coach availability and location existence.</li>
 *   <li><b>Business Rules</b> — Managing configurable rules (e.g. cancellation windows, waitlist
 *       sizes, booking limits) with global defaults and per-location overrides. Publishes
 *       {@code BusinessRuleUpdatedEvent} so other modules can react to changes.</li>
 *   <li><b>Metrics & Analytics</b> — Recording real-time business metrics via event listeners and
 *       nightly aggregation, with support for daily/weekly/monthly granularity, location filtering,
 *       and trend comparisons.</li>
 * </ul>
 *
 * <p>Exposes {@link com.nickdferrara.fitify.admin.AdminApi} for cross-module access to business
 * rule values. Integrates with the Scheduling, Coaching, Location, and Subscription modules
 * through their public APIs.
 */
package com.nickdferrara.fitify.admin;
