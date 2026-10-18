import React, { useState, useEffect } from 'react';
import { BookOpen, CheckSquare, Calendar, Clock, Grid, Settings, Plus, Flame, Award, Trash2, CheckCircle2, Circle } from 'lucide-react';

interface Subject {
  id: string;
  name: string;
  colorHex: string;
}

interface Lecture {
  id: string;
  subjectId: string;
  chapter: string;
  title: string;
  durationMinutes: number;
  status: 'pending' | 'in_progress' | 'done';
  priority: 'high' | 'medium' | 'low';
}

const DEFAULT_SUBJECTS: Subject[] = [
  { id: 'sub_phy', name: 'Physics', colorHex: '#3B82F6' },
  { id: 'sub_bot', name: 'Botany', colorHex: '#10B981' },
  { id: 'sub_zoo', name: 'Zoology', colorHex: '#F59E0B' },
  { id: 'sub_pchem', name: 'Physical Chem', colorHex: '#EC4899' },
  { id: 'sub_ichem', name: 'Inorganic Chem', colorHex: '#8B5CF6' },
  { id: 'sub_ochem', name: 'Organic Chem', colorHex: '#06B6D4' }
];

const INITIAL_LECTURES: Lecture[] = [
  {
    id: '1',
    subjectId: 'sub_phy',
    chapter: 'Kinematics 1D & 2D',
    title: 'Lec 01 - Vectors, Differentiation & Integration',
    durationMinutes: 90,
    status: 'done',
    priority: 'high'
  },
  {
    id: '2',
    subjectId: 'sub_phy',
    chapter: 'Kinematics 1D & 2D',
    title: 'Lec 02 - Projectile Motion on Horizontal & Incline',
    durationMinutes: 85,
    status: 'pending',
    priority: 'high'
  },
  {
    id: '3',
    subjectId: 'sub_bot',
    chapter: 'Cell: The Unit of Life',
    title: 'Lec 01 - Cell Theory, Prokaryotic vs Eukaryotic',
    durationMinutes: 70,
    status: 'done',
    priority: 'medium'
  },
  {
    id: '4',
    subjectId: 'sub_zoo',
    chapter: 'Human Physiology: Digestion',
    title: 'Lec 01 - Dental Formula & Histology of Gut',
    durationMinutes: 60,
    status: 'done',
    priority: 'medium'
  },
  {
    id: '5',
    subjectId: 'sub_pchem',
    chapter: 'Chemical Kinetics',
    title: 'Lec 01 - Rate Laws, Order & Molecularity',
    durationMinutes: 90,
    status: 'pending',
    priority: 'high'
  }
];

export default function App() {
  const [tab, setTab] = useState<'home' | 'backlog' | 'planner' | 'timer' | 'menu'>('home');
  const [lectures, setLectures] = useState<Lecture[]>(() => {
    const saved = localStorage.getItem('kaizen_lectures');
    return saved ? JSON.parse(saved) : INITIAL_LECTURES;
  });
  const [subjects] = useState<Subject[]>(DEFAULT_SUBJECTS);
  const [newTitle, setNewTitle] = useState('');
  const [newChapter, setNewChapter] = useState('');
  const [newSubjectId, setNewSubjectId] = useState('sub_phy');
  const [newDuration, setNewDuration] = useState(60);

  // Timer state
  const [timerSeconds, setTimerSeconds] = useState(50 * 60);
  const [timerActive, setTimerActive] = useState(false);

  useEffect(() => {
    localStorage.setItem('kaizen_lectures', JSON.stringify(lectures));
  }, [lectures]);

  useEffect(() => {
    let interval: any = null;
    if (timerActive && timerSeconds > 0) {
      interval = setInterval(() => {
        setTimerSeconds(s => s - 1);
      }, 1000);
    } else if (timerSeconds === 0) {
      setTimerActive(false);
    }
    return () => clearInterval(interval);
  }, [timerActive, timerSeconds]);

  const toggleStatus = (id: string) => {
    setLectures(prev =>
      prev.map(l =>
        l.id === id ? { ...l, status: l.status === 'done' ? 'pending' : 'done' } : l
      )
    );
  };

  const deleteLecture = (id: string) => {
    setLectures(prev => prev.filter(l => l.id !== id));
  };

  const addLecture = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim()) return;
    const item: Lecture = {
      id: Date.now().toString(),
      subjectId: newSubjectId,
      chapter: newChapter.trim() || 'General Chapter',
      title: newTitle.trim(),
      durationMinutes: Number(newDuration) || 60,
      status: 'pending',
      priority: 'high'
    };
    setLectures([item, ...lectures]);
    setNewTitle('');
    setNewChapter('');
  };

  const completedCount = lectures.filter(l => l.status === 'done').length;
  const pendingCount = lectures.filter(l => l.status !== 'done').length;
  const totalMinutes = lectures.reduce((acc, l) => acc + l.durationMinutes, 0);
  const doneMinutes = lectures.filter(l => l.status === 'done').reduce((acc, l) => acc + l.durationMinutes, 0);
  const progressPercent = lectures.length ? Math.round((completedCount / lectures.length) * 100) : 0;

  const formatTimer = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  return (
    <div style={{ maxWidth: '960px', margin: '0 auto', padding: '16px 20px', flex: 1, width: '100%' }}>
      {/* Top Header */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '26px', fontWeight: 800, color: '#14B8A6' }}>Kaizen</span>
            <span style={{ fontSize: '11px', background: '#1E2846', padding: '2px 8px', borderRadius: '12px', color: '#94A3B8' }}>NEET 2028</span>
          </div>
          <p style={{ fontSize: '13px', color: '#94A3B8', marginTop: '2px' }}>Personal study backlog & daily revision planner</p>
        </div>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', background: '#12182C', padding: '6px 12px', borderRadius: '16px', border: '1px solid #232D4B' }}>
            <Flame size={16} color="#F59E0B" />
            <span style={{ fontSize: '13px', fontWeight: 600 }}>14 Days Streak</span>
          </div>
        </div>
      </header>

      {/* Navigation Bar */}
      <nav style={{ display: 'flex', gap: '8px', borderBottom: '1px solid #232D4B', paddingBottom: '12px', marginBottom: '24px', overflowX: 'auto' }}>
        <button
          onClick={() => setTab('home')}
          style={{
            background: tab === 'home' ? '#14B8A6' : '#12182C',
            color: tab === 'home' ? '#000' : '#F8FAFC',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '12px',
            cursor: 'pointer',
            fontWeight: 600,
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}
        >
          <BookOpen size={16} /> Home
        </button>
        <button
          onClick={() => setTab('backlog')}
          style={{
            background: tab === 'backlog' ? '#14B8A6' : '#12182C',
            color: tab === 'backlog' ? '#000' : '#F8FAFC',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '12px',
            cursor: 'pointer',
            fontWeight: 600,
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}
        >
          <CheckSquare size={16} /> Backlog ({pendingCount})
        </button>
        <button
          onClick={() => setTab('planner')}
          style={{
            background: tab === 'planner' ? '#14B8A6' : '#12182C',
            color: tab === 'planner' ? '#000' : '#F8FAFC',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '12px',
            cursor: 'pointer',
            fontWeight: 600,
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}
        >
          <Calendar size={16} /> Planner
        </button>
        <button
          onClick={() => setTab('timer')}
          style={{
            background: tab === 'timer' ? '#14B8A6' : '#12182C',
            color: tab === 'timer' ? '#000' : '#F8FAFC',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '12px',
            cursor: 'pointer',
            fontWeight: 600,
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}
        >
          <Clock size={16} /> Study Timer
        </button>
      </nav>

      {/* Main Tab Content */}
      {tab === 'home' && (
        <div>
          {/* Metrics Overview */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '24px' }}>
            <div style={{ background: '#12182C', padding: '16px', borderRadius: '16px', border: '1px solid #232D4B' }}>
              <div style={{ fontSize: '12px', color: '#94A3B8' }}>Overall Completion</div>
              <div style={{ fontSize: '28px', fontWeight: 700, color: '#14B8A6', margin: '4px 0' }}>{progressPercent}%</div>
              <div style={{ fontSize: '12px', color: '#64748B' }}>{completedCount} of {lectures.length} completed</div>
            </div>
            <div style={{ background: '#12182C', padding: '16px', borderRadius: '16px', border: '1px solid #232D4B' }}>
              <div style={{ fontSize: '12px', color: '#94A3B8' }}>Pending Backlog</div>
              <div style={{ fontSize: '28px', fontWeight: 700, color: '#F43F5E', margin: '4px 0' }}>{pendingCount}</div>
              <div style={{ fontSize: '12px', color: '#64748B' }}>lectures requiring review</div>
            </div>
            <div style={{ background: '#12182C', padding: '16px', borderRadius: '16px', border: '1px solid #232D4B' }}>
              <div style={{ fontSize: '12px', color: '#94A3B8' }}>Hours Mastered</div>
              <div style={{ fontSize: '28px', fontWeight: 700, color: '#6366F1', margin: '4px 0' }}>{(doneMinutes / 60).toFixed(1)}h</div>
              <div style={{ fontSize: '12px', color: '#64748B' }}>out of {(totalMinutes / 60).toFixed(1)}h target</div>
            </div>
          </div>

          {/* Subjects Grid */}
          <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '12px' }}>Subjects Status</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '12px', marginBottom: '28px' }}>
            {subjects.map(s => {
              const subLecs = lectures.filter(l => l.subjectId === s.id);
              const done = subLecs.filter(l => l.status === 'done').length;
              return (
                <div key={s.id} style={{ background: '#12182C', border: `1px solid ${s.colorHex}44`, borderLeft: `4px solid ${s.colorHex}`, borderRadius: '12px', padding: '12px' }}>
                  <div style={{ fontSize: '14px', fontWeight: 600 }}>{s.name}</div>
                  <div style={{ fontSize: '12px', color: '#94A3B8', marginTop: '4px' }}>{done}/{subLecs.length} Done</div>
                </div>
              );
            })}
          </div>

          {/* Recent Lectures */}
          <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '12px' }}>Today's Priority Backlog</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {lectures.slice(0, 4).map(l => (
              <div key={l.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: '#12182C', border: '1px solid #232D4B', borderRadius: '12px', padding: '12px 16px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <button onClick={() => toggleStatus(l.id)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: l.status === 'done' ? '#10B981' : '#64748B' }}>
                    {l.status === 'done' ? <CheckCircle2 size={20} /> : <Circle size={20} />}
                  </button>
                  <div>
                    <div style={{ fontSize: '14px', fontWeight: 600, textDecoration: l.status === 'done' ? 'line-through' : 'none', color: l.status === 'done' ? '#64748B' : '#F8FAFC' }}>
                      {l.title}
                    </div>
                    <div style={{ fontSize: '12px', color: '#94A3B8' }}>{l.chapter} • {l.durationMinutes} min</div>
                  </div>
                </div>
                <button onClick={() => deleteLecture(l.id)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: '#F43F5E' }}>
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {tab === 'backlog' && (
        <div>
          {/* Add Lecture Form */}
          <form onSubmit={addLecture} style={{ background: '#12182C', border: '1px solid #232D4B', borderRadius: '16px', padding: '16px', marginBottom: '24px' }}>
            <h3 style={{ fontSize: '15px', fontWeight: 600, marginBottom: '12px', color: '#14B8A6' }}>Add New Lecture to Backlog</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '12px', marginBottom: '12px' }}>
              <select
                value={newSubjectId}
                onChange={e => setNewSubjectId(e.target.value)}
                style={{ background: '#1E2846', border: '1px solid #232D4B', color: '#F8FAFC', padding: '8px 12px', borderRadius: '8px' }}
              >
                {subjects.map(s => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
              <input
                type="text"
                placeholder="Chapter name (e.g. Optics)"
                value={newChapter}
                onChange={e => setNewChapter(e.target.value)}
                style={{ background: '#1E2846', border: '1px solid #232D4B', color: '#F8FAFC', padding: '8px 12px', borderRadius: '8px' }}
              />
              <input
                type="number"
                placeholder="Duration (minutes)"
                value={newDuration}
                onChange={e => setNewDuration(Number(e.target.value))}
                style={{ background: '#1E2846', border: '1px solid #232D4B', color: '#F8FAFC', padding: '8px 12px', borderRadius: '8px' }}
              />
            </div>
            <div style={{ display: 'flex', gap: '8px' }}>
              <input
                type="text"
                placeholder="Lecture title (e.g. Lec 03 - Wave Optics & Interference)"
                value={newTitle}
                onChange={e => setNewTitle(e.target.value)}
                style={{ flex: 1, background: '#1E2846', border: '1px solid #232D4B', color: '#F8FAFC', padding: '8px 12px', borderRadius: '8px' }}
              />
              <button
                type="submit"
                style={{ background: '#14B8A6', color: '#000', border: 'none', padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px' }}
              >
                <Plus size={16} /> Add
              </button>
            </div>
          </form>

          {/* Full List */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {lectures.map(l => {
              const sub = subjects.find(s => s.id === l.subjectId);
              return (
                <div key={l.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: '#12182C', border: '1px solid #232D4B', borderRadius: '12px', padding: '12px 16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <button onClick={() => toggleStatus(l.id)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: l.status === 'done' ? '#10B981' : '#64748B' }}>
                      {l.status === 'done' ? <CheckCircle2 size={20} /> : <Circle size={20} />}
                    </button>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ fontSize: '11px', background: `${sub?.colorHex || '#3B82F6'}33`, color: sub?.colorHex || '#3B82F6', padding: '2px 6px', borderRadius: '4px', fontWeight: 600 }}>
                          {sub?.name || 'Subject'}
                        </span>
                        <span style={{ fontSize: '14px', fontWeight: 600, textDecoration: l.status === 'done' ? 'line-through' : 'none', color: l.status === 'done' ? '#64748B' : '#F8FAFC' }}>
                          {l.title}
                        </span>
                      </div>
                      <div style={{ fontSize: '12px', color: '#94A3B8', marginTop: '2px' }}>{l.chapter} • {l.durationMinutes} min</div>
                    </div>
                  </div>
                  <button onClick={() => deleteLecture(l.id)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: '#F43F5E' }}>
                    <Trash2 size={16} />
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {tab === 'planner' && (
        <div style={{ background: '#12182C', border: '1px solid #232D4B', borderRadius: '16px', padding: '24px' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '8px' }}>Catch-up Forecast & Study Schedule</h2>
          <p style={{ fontSize: '13px', color: '#94A3B8', marginBottom: '20px' }}>
            Based on a daily goal of 6 hours of focused study, your backlog will be cleared in{' '}
            <strong style={{ color: '#14B8A6' }}>
              {Math.ceil(pendingCount > 0 ? (pendingCount * 80) / (6 * 60) : 0)} days
            </strong>.
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
            {['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'].map((day, idx) => (
              <div key={day} style={{ background: '#1E2846', padding: '12px', borderRadius: '12px', border: '1px solid #232D4B' }}>
                <div style={{ fontSize: '14px', fontWeight: 700, color: '#F8FAFC' }}>{day}</div>
                <div style={{ fontSize: '12px', color: '#14B8A6', margin: '4px 0' }}>2 Lectures scheduled</div>
                <div style={{ fontSize: '11px', color: '#94A3B8' }}>Physics & Zoology</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {tab === 'timer' && (
        <div style={{ background: '#12182C', border: '1px solid #232D4B', borderRadius: '16px', padding: '36px', textAlign: 'center', maxWidth: '440px', margin: '0 auto' }}>
          <div style={{ fontSize: '13px', color: '#94A3B8', textTransform: 'uppercase', letterSpacing: '1px', fontWeight: 600 }}>Focused Study Session</div>
          <div style={{ fontSize: '64px', fontWeight: 800, color: '#14B8A6', margin: '16px 0', fontFamily: 'monospace' }}>
            {formatTimer(timerSeconds)}
          </div>
          <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
            <button
              onClick={() => setTimerActive(!timerActive)}
              style={{ background: timerActive ? '#EF4444' : '#14B8A6', color: '#000', border: 'none', padding: '12px 28px', borderRadius: '12px', cursor: 'pointer', fontWeight: 700, fontSize: '15px' }}
            >
              {timerActive ? 'Pause' : 'Start Focus'}
            </button>
            <button
              onClick={() => { setTimerActive(false); setTimerSeconds(50 * 60); }}
              style={{ background: '#1E2846', color: '#F8FAFC', border: '1px solid #232D4B', padding: '12px 20px', borderRadius: '12px', cursor: 'pointer', fontWeight: 600, fontSize: '14px' }}
            >
              Reset
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
