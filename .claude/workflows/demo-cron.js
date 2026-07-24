export const meta = {
  name: 'demo-cron',
  description: 'Écrit "bonjour Mouhssine et kahina" une seule fois par exécution',
  whenToUse: 'Déclenché par le cron toutes les 2 minutes pour réécrire le message',
  phases: [{ title: 'Bonjour' }],
}

phase('Bonjour')
log('bonjour Mouhssine et kahina')
return 'bonjour Mouhssine et kahina'
