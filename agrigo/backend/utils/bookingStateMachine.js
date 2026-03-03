const transitions = {
  pending: ['accepted', 'rejected'],
  accepted: ['ongoing', 'rejected'],
  ongoing: ['completed'],
  completed: [],
  rejected: [],
};

const canTransition = (from, to) => transitions[from]?.includes(to) || false;

module.exports = { transitions, canTransition };
